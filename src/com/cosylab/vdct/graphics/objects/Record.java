package com.cosylab.vdct.graphics.objects;

import java.awt.*;
import java.util.*;
import com.cosylab.vdct.Constants;
import com.cosylab.vdct.graphics.*;
import com.cosylab.vdct.vdb.*;
import com.cosylab.vdct.dbd.DBDConstants;

import com.cosylab.vdct.inspector.*;

import com.cosylab.vdct.graphics.popup.*;
import javax.swing.*;
import java.awt.event.*;
import com.cosylab.vdct.events.*;
import com.cosylab.vdct.events.commands.*;

/**
 * Insert the type's description here.
 * Creation date: (21.12.2000 20:46:35)
 * @author: Matej Sekoranja
 */
public class Record extends ContainerObject implements Clipboardable, Descriptable, Flexible, Hub, Morphable, Movable, MultiInLink, Rotatable, Selectable, Popupable, Inspectable {

	class PopupMenuHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
		    LinkCommand cmd = (LinkCommand)CommandManager.getInstance().getCommand("LinkCommand");
		    cmd.setData(Record.this, recordData.getField(e.getActionCommand()));
	 		cmd.execute();
		}
	}
	private final static String nullString = "";
	private final static String fieldMaxStr = "01234567890123456789012345";
	private final static int tailSizeOfR = 4;
	private static javax.swing.ImageIcon icon = null;
	protected VDBRecordData recordData = null;
	// type label
	protected int rtypeLabelX;
	protected int rtypeLabelY;
	protected String label2;
	protected Font typeFont = null;
	// changed fields label
	protected int rfieldLabelX;
	protected int rfieldLabelY;
	protected int rfieldRowHeight;
	protected Font fieldFont = null;
	protected Vector changedFields;
	protected Vector outlinks;
	protected boolean disconnected = false;
	private boolean right = true;
	private boolean target = false;
	private final static String inlinkString = "INLINK";
	private final static String outlinkString = "OUTLINK";
	private final static String fwdlinkString = "FWDLINK";
	private final static String varlinkString = "VARIABLE";
/**
 * Group constructor comment.
 * @param parent com.cosylab.vdct.graphics.objects.ContainerObject
 */
public Record(ContainerObject parent, VDBRecordData recordData, int x, int y) {
	super(parent);
	this.recordData=recordData;
	setColor(Color.black);
	setWidth(Constants.RECORD_WIDTH);
	setHeight(Constants.RECORD_HEIGHT);
	setX(x); setY(y);

	changedFields = new Vector();
	outlinks = new Vector();

	VDBFieldData field;
	Enumeration e = recordData.getFieldsV().elements();
	while (e.hasMoreElements()) {
		field = (VDBFieldData)e.nextElement();
		if (!field.hasDefaultValue())
			changedFields.addElement(field);
	}

	forceValidation();
	
}
/**
 * Insert the method's description here.
 * Creation date: (5.2.2001 13:36:25)
 * @param oldRecordName java.lang.String
 * @param newRecordName java.lang.String
 */
public void _fixEPICSInLinks(String oldRecordName, String newRecordName) {
	if (oldRecordName.equals(newRecordName)) return;
	
	Object obj; String old;
	EPICSLinkOut outlink;
	Enumeration fields = getSubObjectsV().elements();
	Enumeration outs;
	while (fields.hasMoreElements()) {
		obj = fields.nextElement();
		if (obj instanceof EPICSVarLink) {
			outs = ((EPICSVarLink)obj).getStartPoints().elements();
			while (outs.hasMoreElements()) {
				obj = outs.nextElement();
				if (obj instanceof EPICSLinkOut) {
					outlink = (EPICSLinkOut)obj;
					old = outlink.getFieldData().getValue();
					if (old.startsWith(oldRecordName))
						outlink.getFieldData().setValue(newRecordName+old.substring(oldRecordName.length()));
						
				}
			}
		}					
	}

	// fix record inlink
	outs = getStartPoints().elements();
	while (outs.hasMoreElements()) {
		obj = outs.nextElement();
		if (obj instanceof EPICSLinkOut) {
			outlink = (EPICSLinkOut)obj;
			old = outlink.getFieldData().getValue();
			if (old.startsWith(oldRecordName))
				outlink.getFieldData().setValue(newRecordName+old.substring(oldRecordName.length()));
				
		}
	}
  	
}
/**
 * Insert the method's description here.
 * Creation date: (21.12.2000 20:46:35)
 * @param visitor com.cosylab.vdct.graphics.objects.Visitor
 */
public void accept(Visitor visitor) {
	visitor.visitGroup();
}
/**
 * Insert the method's description here.
 * Creation date: (29.1.2001 22:40:48)
 * @param link com.cosylab.vdct.graphics.objects.Linkable
 */
public void addLink(Linkable link) {
	if (!getSubObjectsV().contains(link)) {
		Field field = (Field)link;
		addSubObject(field.getFieldData().getName(), field);
		validateFields();
		revalidateFieldsPosition();
	}
}
/**
 * Insert the method's description here.
 * Creation date: (25.12.2000 14:14:35)
 * @return boolean
 * @param dx int
 * @param dy int
 */
public boolean checkMove(int dx, int dy) {
	ViewState view = ViewState.getInstance();

	if ((getX()<-dx) || (getY()<-dy) || 
		(getX()>(view.getWidth()-getWidth()-dx)) || (getY()>(view.getHeight()-getHeight()-dy)))
		return false;
	else
		return true;
}
/**
 * Insert the method's description here.
 * Creation date: (4.2.2001 22:02:29)
 * @param group java.lang.String
 */
public boolean copyToGroup(java.lang.String group) {

	String newName;
	if (group.equals(nullString))
		newName = Group.substractObjectName(recordData.getName());
	else
		newName = group+Constants.GROUP_SEPARATOR+
				  Group.substractObjectName(recordData.getName());

	// object with new name already exists, add suffix ///!!!
	//Object obj;
	while (Group.getRoot().findObject(newName, true)!=null)
		newName += Constants.COPY_SUFFIX;

	ViewState view = ViewState.getInstance();


	VDBRecordData theDataCopy = VDBData.copyVDBRecordData(recordData);
	theDataCopy.setName(newName);
	Record theRecordCopy = new Record(null, theDataCopy, getX(), getY());
	theRecordCopy.move(20-view.getRx(), 20-view.getRy());
	Group.getRoot().addSubObject(theDataCopy.getName(), theRecordCopy, true);
	//theRecordCopy.fixEPICSOutLinks(Group.substractParentName(recordData.getName()), group);
	theRecordCopy.manageLinks();
	unconditionalValidation();

	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (2.2.2001 23:00:51)
 * @return com.cosylab.vdct.graphics.objects.Record.PopupMenuHandler
 */
private com.cosylab.vdct.graphics.objects.Record.PopupMenuHandler createPopupmenuHandler() {
	return new PopupMenuHandler();
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:59:21)
 */
public void destroy() {
	if (!isDestroyed()) {
		super.destroy();
		destroyFields();
//		disconnected=true;
		
		if (outlinks.size()>0) {
			Object[] objs = new Object[outlinks.size()];
			outlinks.copyInto(objs);
			for(int i=0; i<objs.length; i++) {
				OutLink outlink = (OutLink)objs[i];
				OutLink start = EPICSLinkOut.getStartPoint(outlink);
				if(start instanceof EPICSLinkOut)
					((EPICSLinkOut)start).destroy();
				else if (start!=null)
					start.disconnect(this);
				else 
					outlink.disconnect(this);
			}
		}
		
		clear();
		getParent().removeObject(Group.substractObjectName(getName()));
	}
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:59:54)
 */
private void destroyFields() {

	Object[] objs = new Object[subObjectsV.size()];
	subObjectsV.copyInto(objs);
	for (int i=0; i < objs.length; i++)
		((VisibleObject)objs[i]).destroy();
	
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 */
public void disconnect(Linkable disconnector) {
	if (!disconnected && outlinks.contains(disconnector)) {
		outlinks.removeElement(disconnector);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (21.12.2000 20:46:35)
 * @param g java.awt.Graphics
 * @param hilited boolean
 */
protected void draw(Graphics g, boolean hilited) {

	ViewState view = ViewState.getInstance();

	int rrx = getRx() - view.getRx();
	int rry = getRy() - view.getRy();

	int rwidth = getRwidth();
	int rheight = getRheight();

	// clipping
	if (!((rrx > view.getViewWidth())
		|| (rry > view.getViewHeight())
		|| ((rrx + rwidth) < 0)
		|| ((rry + rheight) < 0))) {

		if (!hilited)
			g.setColor(Constants.RECORD_COLOR);
		else
			if (view.isPicked(this))
				g.setColor(Constants.PICK_COLOR);
			else
				if (view.isSelected(this) || view.isBlinking(this))
					g.setColor(Constants.SELECTION_COLOR);
				else
					g.setColor(Constants.RECORD_COLOR);

		g.fillRect(rrx, rry, rwidth, rheight);
		if (!hilited)
			g.setColor(Constants.FRAME_COLOR);
		else
			g.setColor(
				(this == view.getHilitedObject())
					? Constants.HILITE_COLOR
					: Constants.FRAME_COLOR);

		g.drawRect(rrx, rry, rwidth, rheight);

		// middle line
		int ox = (int) (10 * getRscale());
		int ly = (int) (rry + Constants.RECORD_HEIGHT * getRscale());
		g.drawLine(rrx + ox, ly, rrx + rwidth - ox, ly);

		if (getFont() != null) {
			g.setFont(getFont());
			g.drawString(getLabel(), rrx + getRlabelX(), rry + getRlabelY());
		}

		if (typeFont != null) {
			g.setFont(typeFont);
			g.drawString(label2, rrx + rtypeLabelX, rry + rtypeLabelY);
		}

		if (fieldFont != null) {
			g.setFont(fieldFont);
			FontMetrics fm = FontMetricsBuffer.getInstance().getFontMetrics(fieldFont);
			String val;
			VDBFieldData fd;
			int px = rrx + rfieldLabelX;
			int py = rry + rfieldLabelY;
			Enumeration e = changedFields.elements();
			while (e.hasMoreElements()) {
				fd = (VDBFieldData) (e.nextElement());
				val = fd.getName() + "=" + fd.getValue();
				while ((fm.stringWidth(val) + ox) > rwidth)
					val = val.substring(0, val.length() - 2);
				g.drawString(val, px, py);
				py += rfieldRowHeight;
			}
		}

		// fwdlink support
		if (!disconnected && (outlinks.size() > 0)) {

			Color recordColor = g.getColor();
			Color linkColor = recordColor;
			if (outlinks.firstElement() instanceof VisibleObject)
				linkColor = ((VisibleObject) outlinks.firstElement()).getColor();

			// draw link and its tail
			boolean isRightSide = isRight();
			int r = (int) (Constants.LINK_RADIOUS * getRscale());
			int cy = rry + rheight / 2;

			int cx;
			if (isRightSide) {
				cx = rrx + rwidth + r;
				g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
				g.setColor(linkColor);
				g.drawLine(cx + 2 * r, cy, cx + (2 + tailSizeOfR) * r, cy);
			} else {
				cx = rrx - r;
				g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
				g.setColor(linkColor);
				g.drawLine(cx - (2 + tailSizeOfR) * r, cy, cx - 2 * r, cy);
			}

			// !!! more intergroup inlinks?!
			LinkDrawer.drawInIntergroupLink(
				g,
				(OutLink) outlinks.firstElement(),
				this,
				isRightSide);
		}

	}

	paintSubObjects(g, hilited);
}
/**
 * Insert the method's description here.
 * Creation date: (27.1.2001 16:12:03)
 * @param field com.cosylab.vdct.vdb.VDBFieldData
 */
public void fieldChanged(VDBFieldData field) {
	boolean repaint = false;

	if (manageLink(field)) repaint=true;
		
	if (field.hasDefaultValue()) {
		if (changedFields.contains(field)) {
				changedFields.removeElement(field);
				repaint = true;
		}
				
	}
	else {
		if (!changedFields.contains(field))
				changedFields.addElement(field);
		repaint=true;
	}
	if (repaint) {
		unconditionalValidation();
		com.cosylab.vdct.events.CommandManager.getInstance().execute("RepaintWorkspace");
	}
}
/**
 * Insert the method's description here.
 * Creation date: (5.2.2001 9:42:29)
 * @param prevGroup java.lang.String
 * @param group java.lang.String
 */
public void fixEPICSOutLinks(String prevGroup, String group) {
	if (prevGroup.equals(group)) return;
	
	String prefix;
	if (group.equals(nullString)) prefix=nullString;
	else prefix=group+Constants.GROUP_SEPARATOR;

	String old; 
	int type; VDBFieldData field;
	Enumeration e = recordData.getFieldsV().elements();
	while (e.hasMoreElements()) {
		field = (VDBFieldData)e.nextElement();
		type = LinkProperties.getType(field);
		if (type != LinkProperties.VARIABLE_FIELD) {
			old = field.getValue();
			if (!old.equals(nullString) && !old.startsWith(Constants.HARDWARE_LINK) &&
				old.startsWith(prevGroup)) {
				if (prevGroup.equals(nullString))
					field.setValue(prefix+old);
				else
					field.setValue(prefix+old.substring(prevGroup.length()+1));
			}
		}
	}

}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 8:37:37)
 */
private void fixForwardLinks() {

	String targetName = getRecordData().getName();
	EPICSLinkOut source;
	Object unknownLink;
	Enumeration e = this.getStartPoints().elements();
	while (e.hasMoreElements())
	{
		unknownLink = e.nextElement();
		if (unknownLink instanceof EPICSLinkOut) 
				source = (EPICSLinkOut)unknownLink;  
			else
				continue;	// nothing to fix
		
		// now I got source and target, compare values
		String oldTarget = LinkProperties.getTarget(source.getFieldData());
		if (!oldTarget.equalsIgnoreCase(targetName))
		{
			// not the same, fix it gently as a doctor :)
			String value = source.getFieldData().getValue();
			value = targetName + com.cosylab.vdct.util.StringUtils.removeBegining(value, oldTarget);
			source.getFieldData().setValueSilently(value);
			source.fixLinkProperties();
		}
		
	}
			
}
/**
 * Goes through link fields (in, out, var, fwd) and cheks
 * if ther are OK, if not it fixes it
 * When record is moved, renames, etc. value of in, out, fwd
 * should be changed, but visual link is still preserved :)
 * (linked list). It compares start point end end point and ...
 * Creation date: (2.5.2001 19:37:46)
 */
public void fixLinks() {

	// links to this record
	fixForwardLinks();
	
	Object unknownField;
	EPICSLinkOut source;
	EPICSVarLink varlink;
	String targetName;
	
	Enumeration e = getSubObjectsV().elements();
	while (e.hasMoreElements())
	{
		unknownField = e.nextElement();
			
			// go and find source
			if (unknownField instanceof EPICSVarLink)
			{
				varlink = (EPICSVarLink)unknownField;
				targetName = varlink.getFieldData().getFullName();

				
				Enumeration e2 = varlink.getStartPoints().elements();
				while (e2.hasMoreElements())
				{
					unknownField = e2.nextElement();
					if (unknownField instanceof EPICSLinkOut) 
						source = (EPICSLinkOut)unknownField;  
					else
						continue;	// nothing to fix

		
					// now I got source and target, compare values
					String oldTarget = LinkProperties.getTarget(source.getFieldData());
					if (!oldTarget.equalsIgnoreCase(targetName))
					{
						// not the same, fix it gently as a doctor :)
						String value = source.getFieldData().getValue();
						value = targetName + com.cosylab.vdct.util.StringUtils.removeBegining(value, oldTarget);
						source.getFieldData().setValueSilently(value);
						source.fixLinkProperties();
					}
				}
			}

/*
			else if (unknownField instanceof EPICSLinkOut)
			{
				source = (EPICSLinkOut)unknownField;
				InLink inlink = EPICSLinkOut.getEndPoint(source);
				if (inlink!=null && inlink instanceof EPICSVarLink)
				{
					varlink = (EPICSVarLink)inlink;
					targetName = varlink.getFieldData().getFullName();
					// now I got source and target, compare values
					String oldTarget = LinkProperties.getTarget(source.getFieldData());
					if (!oldTarget.equalsIgnoreCase(targetName))
					{
						// not the same, fix it gently as a doctor :)
						String value = source.getFieldData().getValue();
						value = targetName + com.cosylab.vdct.util.StringUtils.removeBegining(value, oldTarget);
						source.getFieldData().setValueSilently(value);
						source.fixLinkProperties();
					}
				}
			}

*/
			
	}
	
}
/**
 * Insert the method's description here.
 * Creation date: (26.1.2001 15:00:15)
 * @return com.cosylab.vdct.inspector.InspectableProperty
 */
public com.cosylab.vdct.inspector.InspectableProperty getCommentProperty() {
	return new CommentProperty(recordData);
}
/**
 * Insert the method's description here.
 * Creation date: (9.4.2001 13:12:33)
 * @return java.lang.String
 */
public java.lang.String getDescription() {
	return getName();
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 10:16:55)
 * @return java.lang.String
 */
public java.lang.String getFlexibleName() {
	return recordData.getName();
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 16:41:13)
 * @return java.lang.String
 */
public java.lang.String getHashID() {
	return Group.substractObjectName(getName());
}
/**
 * Insert the method's description here.
 * Creation date: (25.4.2001 17:58:03)
 * @return int
 */
public int getHeight() {
	forceValidation();
	return super.getHeight();
}
/**
 * Insert the method's description here.
 * Creation date: (10.1.2001 15:15:51)
 * @return javax.swing.Icon
 */
public javax.swing.Icon getIcon() {
	if (icon==null)
		icon = new javax.swing.ImageIcon(getClass().getResource("/images/record.gif"));
	return icon;
}
/**
 * Insert the method's description here.
 * Creation date: (23.4.2001 20:37:11)
 * @return java.lang.String
 */
public String getID() {
	return getName();
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:54)
 * @return int
 */
public int getInX() {
	if (isRight())
		return getX()+getWidth()+(tailSizeOfR+3)*Constants.LINK_RADIOUS;
	else
		return getX()-(tailSizeOfR+3)*Constants.LINK_RADIOUS;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:54)
 * @return int
 */
public int getInY() {
	return getY()+getHeight()/2;
}
/**
 * Insert the method's description here.
 * Creation date: (2.2.2001 20:31:29)
 * @return java.util.Vector
 */
public Vector getItems() {
	Vector items = new Vector();
	ActionListener l = createPopupmenuHandler();
	VDBFieldData field;
	JMenuItem menuitem;
	
	if (isTarget()) {
		int count = 0;
		JMenu varlinkItem = new JMenu(varlinkString);
		JMenu menu = varlinkItem;
		
		Enumeration e = recordData.getFieldsV().elements();
		while (e.hasMoreElements()) {
			field = (VDBFieldData)(e.nextElement());
/*			switch (field.getType()) {
				case DBDConstants.DBF_CHAR: 
				case DBDConstants.DBF_UCHAR: 
				case DBDConstants.DBF_SHORT: 
				case DBDConstants.DBF_USHORT: 
				case DBDConstants.DBF_LONG: 
				case DBDConstants.DBF_ULONG: 
				case DBDConstants.DBF_FLOAT: 
				case DBDConstants.DBF_DOUBLE: 
				case DBDConstants.DBF_STRING:
				case DBDConstants.DBF_NOACCESS:		// added by request of APS
				case DBDConstants.DBF_ENUM:
				case DBDConstants.DBF_MENU:
				case DBDConstants.DBF_DEVICE:  // ?
				  menuitem = new JMenuItem(field.getName());
				  menuitem.addActionListener(l);
				  menu = PopUpMenu.addItem(menuitem, menu, count);
				  count++; 
			}
*/
			if (field.getType()!=DBDConstants.DBF_INLINK &&
				field.getType()!=DBDConstants.DBF_OUTLINK &&
				field.getType()!=DBDConstants.DBF_FWDLINK)
			{
				  menuitem = new JMenuItem(field.getName());
				  menuitem.addActionListener(l);
				  menu = PopUpMenu.addItem(menuitem, menu, count);
				  count++; 
			}

		}
		if (count > 0) items.addElement(varlinkItem);
		
	}
	else {
		
		JMenu inlinks = new JMenu(inlinkString);
		JMenu outlinks = new JMenu(outlinkString);
		JMenu fwdlinks = new JMenu(fwdlinkString);
		
		//boolean isSoft = recordData.canBePV_LINK(); !!! can be added

		JMenu inMenu = inlinks;	
		JMenu outMenu = outlinks;	
		JMenu fwdMenu = fwdlinks;	
		
		int inpItems, outItems, fwdItems;
		inpItems=outItems=fwdItems=0;

		Enumeration e = recordData.getFieldsV().elements();
		while (e.hasMoreElements()) {
			field = (VDBFieldData)(e.nextElement());
			if (field.getValue().equals(nullString)) {
				switch (field.getType()) {
					case DBDConstants.DBF_INLINK:
						 menuitem = new JMenuItem(field.getName());
						 menuitem.addActionListener(l);
						 inlinks = PopUpMenu.addItem(menuitem, inlinks, inpItems); 
						 inpItems++;
						 break;
					case DBDConstants.DBF_OUTLINK: 
						 menuitem = new JMenuItem(field.getName());
						 menuitem.addActionListener(l);
						 outlinks = PopUpMenu.addItem(menuitem, outlinks, outItems); 
						 outItems++;
						 break;
					case DBDConstants.DBF_FWDLINK:
						 menuitem = new JMenuItem(field.getName());
						 menuitem.addActionListener(l);
						 fwdlinks = PopUpMenu.addItem(menuitem, fwdlinks, fwdItems); 
						 fwdItems++;
						 break;
				}
			}
		}

		if (inMenu.getItemCount() > 0)
			items.addElement(inMenu);
		if (outMenu.getItemCount() > 0)
			items.addElement(outMenu);
		if (fwdMenu.getItemCount() > 0)
			items.addElement(fwdMenu);

	}
		
	return items;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 * @return java.lang.String
 */
public java.lang.String getLayerID() {
	return getParent().toString();
}
/**
 * Insert the method's description here.
 * Creation date: (4.5.2001 9:54:07)
 * @return java.util.Vector
 */
public int getLinkCount() {
	return outlinks.size();
}
/**
 * Insert the method's description here.
 * Creation date: (2.2.2001 21:40:05)
 * @return java.lang.String
 */
public java.lang.String getName() {
	return recordData.getName();
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 * @return com.cosylab.vdct.graphics.objects.OutLink
 */
public OutLink getOutput() {
	if (outlinks.size()==1)
		return (OutLink)outlinks.firstElement();
	else
		return null;
}
/**
 * Return properties to be inspected
 * Creation date: (11.1.2001 21:43:31)
 * @return com.cosylab.vdct.inspector.InspectableProperty[]
 */
public com.cosylab.vdct.inspector.InspectableProperty[] getProperties() {

	int size = 0;
	VDBFieldData field;	Integer key;
	Hashtable groups = new Hashtable();
	Enumeration e = recordData.getFieldsV().elements();
	while (e.hasMoreElements()) {
		field = (VDBFieldData)e.nextElement();
		if (field.getDbdData().getField_type() != 
			com.cosylab.vdct.dbd.DBDConstants.DBF_NOACCESS) {

			key = new Integer(field.getGUI_type());
			if (groups.containsKey(key)) {
				((Vector)(groups.get(key))).addElement(field);
				size++;
			}
			else {
				Vector v = new Vector();
				v.addElement(field);
				groups.put(key, v);
				size+=2;	// separator + property
			}
		}
	}
	
	Object[] grps;
	grps = new com.cosylab.vdct.util.IntegerQuickSort().sortEnumeration(groups.keys());

	Vector all = new Vector();
	
	Vector items; int grp;
	for (int gn=0; gn < grps.length; gn++) {
		items = (Vector)groups.get(grps[gn]);
		grp = ((VDBFieldData)(items.firstElement())).getGUI_type();
		all.addElement(new GUISeparator(com.cosylab.vdct.dbd.DBDResolver.getGUIString(grp)));
		all.addAll(items);
	}
	
	InspectableProperty[] properties = new InspectableProperty[all.size()];
	all.copyInto(properties);
	return properties;
	
}
/**
 * Insert the method's description here.
 * Creation date: (8.1.2001 21:18:50)
 * @return com.cosylab.vdct.vdb.VDBRecordData
 */
public com.cosylab.vdct.vdb.VDBRecordData getRecordData() {
	return recordData;
}
/**
 * Insert the method's description here.
 * Creation date: (5.2.2001 12:10:18)
 * @return java.util.Vector
 */
public Vector getStartPoints() {
	OutLink out;
	Vector starts = new Vector();
	Enumeration e = outlinks.elements();
	while (e.hasMoreElements()) {
		out = EPICSLinkOut.getStartPoint((Linkable)e.nextElement());
		if (out!=null) starts.addElement(out);
	}
	return starts;
}
/**
 * Insert the method's description here.
 * Creation date: (25.4.2001 22:13:55)
 * @return int
 */
public int getX() {
	int posX = super.getX();
	if (com.cosylab.vdct.Settings.getInstance().getSnapToGrid())
		return posX - posX % Constants.GRID_SIZE;
	else
		return posX;
}
/**
 * Insert the method's description here.
 * Creation date: (25.4.2001 22:13:55)
 * @return int
 */
public int getY() {
	int posY = super.getY();
	if (com.cosylab.vdct.Settings.getInstance().getSnapToGrid())
		return posY - posY % Constants.GRID_SIZE;
	else
		return posY;
}
/**
 * Returned value inicates change
 * Creation date: (21.12.2000 22:21:12)
 * @return com.cosylab.visible.objects.VisibleObject
 * @param x int
 * @param y int
 */
public VisibleObject hiliteComponentsCheck(int x, int y) {

	ViewState view = ViewState.getInstance();
	VisibleObject spotted = null;
	
	Enumeration e = subObjectsV.elements();
	VisibleObject vo;
	while (e.hasMoreElements()) {
		vo = (VisibleObject)(e.nextElement());
		vo = vo.intersects(x, y);
		if (vo!=null) {
			spotted=vo;
			if (view.getHilitedObject()!=vo) return vo;
		}
	}

	return spotted;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:36:15)
 * @param field com.cosylab.vdct.vdb.VDBFieldData
 */
public EPICSLink initializeLinkField(VDBFieldData field) {

	if (!this.containsObject(field.getName()))
	{
		EPICSLink link = null;	
		int type = LinkProperties.getType(field);
		switch (type) {
			case LinkProperties.INLINK_FIELD:
				link = new EPICSInLink(this, field);
				break;
			case  LinkProperties.OUTLINK_FIELD:
				link = new EPICSOutLink(this, field);
				break;
			case LinkProperties.FWDLINK_FIELD:
				link = new EPICSFwdLink(this, field);
				break;
			case LinkProperties.VARIABLE_FIELD:
				link = new EPICSVarLink(this, field);
				break;
		}

		if (link!=null)	addLink(link);
		return link;
	}
	else
		return null;
}
/**
 * Default impmlementation for square (must be rescaled)
 * Creation date: (19.12.2000 20:20:20)
 * @return com.cosylab.visible.objects.VisibleObject
 * @param px int
 * @param py int
 */
public VisibleObject intersects(int px, int py) {
	if ((getRx()<=px) && (getRy()<=py) && 
		((getRx()+getRwidth())>=px) && 
		((getRy()+getRheight())>=py))
		return this;
	else 
		return hiliteComponentsCheck(px, py);
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 * @return boolean
 */
public boolean isConnectable() {
	return !disconnected;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 * @return boolean
 */
public boolean isDisconnected() {
	return disconnected;
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 22:54:43)
 * @return boolean
 * @param field com.cosylab.vdct.graphics.objects.Field
 */
public boolean isFirstField(Field field) {
	// find first field and compare
		
	Enumeration e = subObjectsV.elements();
	Object obj;
	while (e.hasMoreElements()) {
		obj = e.nextElement();
		if (obj instanceof Field)
			if (obj==field)
				return true;
			else
				return false;
	}
	
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 22:53:47)
 * @param field com.cosylab.vdct.graphics.objects.Field
 */
public boolean isLastField(Field field) {
	for (int i= subObjectsV.size()-1; i>=0; i--)
		if (subObjectsV.elementAt(i) instanceof Field)
			if (subObjectsV.elementAt(i)==field)
				return true;
			else
				return false;
	return false;
	
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 16:58:58)
 * @return boolean
 */
public boolean isRight() {
	if (disconnected || outlinks.size()!=1)
		return right;
	else {
		OutLink first = (OutLink)outlinks.firstElement();
		if (first.getLayerID().equals(getLayerID()))
			return (first.getOutX()>(getX()+getWidth()/2));
		else
			return right;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (3.2.2001 13:25:42)
 * @return boolean
 */
public boolean isTarget() {
	return target;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:36:15)
 * @return boolean
 * @param field com.cosylab.vdct.vdb.VDBFieldData
 */
private boolean manageLink(VDBFieldData field) {

	int type = LinkProperties.getType(field);
	if (type == LinkProperties.VARIABLE_FIELD)
	{
		if (this.containsObject(field.getName()))
		{
			EPICSVarLink link = (EPICSVarLink)getSubObject(field.getName());
			link.validateLink();
			return true;			
		}
		return false;
	}	
	else
	{
		
		if (this.containsObject(field.getName()))
		{
			// existing link
			EPICSLinkOut link = (EPICSLinkOut)getSubObject(field.getName());
			link.valueChanged();
			return true;
			
		}
		else
		{
			if (field.getValue().startsWith(Constants.HARDWARE_LINK) ||
				field.getValue().startsWith("@") ||    // !!!??
				field.getValue().equals(nullString) ||
				Character.isDigit(field.getValue().charAt(0))) 
				return false; 	//!!!
			// new link
			LinkProperties properties = new LinkProperties(field);
			InLink varlink = EPICSLinkOut.getTarget(properties);
			// can point to null? OK, cross will be showed

			EPICSLinkOut outlink = null;
			
			if (type==LinkProperties.INLINK_FIELD)
				outlink = new EPICSInLink(this, field);
			else if (type==LinkProperties.OUTLINK_FIELD)
				outlink = new EPICSOutLink(this, field);
			else /*if (type==LinkProperties.FWDLINK_FIELD)*/
				outlink = new EPICSFwdLink(this, field);
		
			addLink(outlink);
			/*if (!properties.isIsInterGroupLink())
			{
				String id = EPICSLinkOut.generateConnectorID(outlink);
				Connector connector = new Connector(id, this, outlink, varlink);
				if (varlink!=null)
				{
					connector.setX((outlink.getOutX()+varlink.getInX())/2);
					connector.setY((outlink.getOutY()+varlink.getInY())/2);
				}
				addSubObject(id, connector);
			}
			else*/
			{
				if (varlink!=null) varlink.setOutput(outlink, null);
				outlink.setInput(varlink);
			}

			return true;
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:35:39)
 */
public void manageLinks() {
	VDBFieldData field;
	Enumeration e = recordData.getFieldsV().elements();
	while (e.hasMoreElements()) {
		field = (VDBFieldData)e.nextElement();
		manageLink(field);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (4.2.2001 21:58:46)
 * @param newType java.lang.String
 */
public void morph(java.lang.String newType) {}
/**
 * Insert the method's description here.
 * Creation date: (25.12.2000 14:14:35)
 * @return boolean
 * @param dx int
 * @param dy int
 */
public boolean move(int dx, int dy) {
	if (checkMove(dx, dy)) {
		setX(super.getX()+dx);
		setY(super.getY()+dy);
		moveConnectors(dx, dy);
		revalidatePosition();
		return true;
	}
	else 
		return false;
}
/**
 * Insert the method's description here.
 * Creation date: (1.2.2001 17:38:36)
 * @param dx int
 * @param dy int
 */
private void moveConnectors(int dx, int dy) {
	
  ViewState view = ViewState.getInstance();
  Enumeration e = subObjectsV.elements();
  Connector con; Object obj;
  while (e.hasMoreElements()) {
	obj = e.nextElement();
	if (obj instanceof Connector) {
		con = (Connector)obj;
		InLink endpoint = EPICSLinkOut.getEndPoint(con);
		/*OutLink startpoint = EPICSLinkOut.getStartPoint(con);
		EPICSLinkOut lo = null;
		if (!(startpoint instanceof EPICSLinkOut))
			lo = (EPICSLinkOut)startpoint;*/
		if (((endpoint instanceof EPICSLink) &&
			(view.isSelected(((EPICSLink)endpoint).getParent())) /*||
			((lo!=null) && lo.getLinkProperties().isIsInterGroupLink())*/)
			||
			((endpoint instanceof Record) && view.isSelected(endpoint)))
			con.move(dx, dy);
	}
  }
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 22:36:11)
 * @param field com.cosylab.vdct.graphics.objects.Field
 */
public void moveFieldDown(Field field) {
	// move visual field
	Vector fields = getSubObjectsV();
	int pos = fields.indexOf(field);

	pos++;
	while (pos<fields.size() && !(fields.elementAt(pos) instanceof Field))
		pos++;

	if (pos<fields.size()) {
		fields.removeElement(field);
		fields.insertElementAt(field, pos);
		revalidateFieldsPosition();
	}
	com.cosylab.vdct.events.CommandManager.getInstance().execute("RepaintWorkspace");
	com.cosylab.vdct.undo.UndoManager.getInstance().addAction(new com.cosylab.vdct.undo.MoveFieldDownAction(field));
}
/**
 * Insert the method's description here.
 * Creation date: (3.5.2001 22:36:11)
 * @param field com.cosylab.vdct.graphics.objects.Field
 */
public void moveFieldUp(Field field) {
	// move visual field
	Vector fields = getSubObjectsV();
	int pos = fields.indexOf(field);
	pos--;
	while (pos>=0 && !(fields.elementAt(pos) instanceof Field))
		pos--;

	if (pos>=0) {
		fields.removeElement(field);
		fields.insertElementAt(field, pos);
		revalidateFieldsPosition();
	}
	
	com.cosylab.vdct.events.CommandManager.getInstance().execute("RepaintWorkspace");
	com.cosylab.vdct.undo.UndoManager.getInstance().addAction(new com.cosylab.vdct.undo.MoveFieldUpAction(field));
}
/**
 * Insert the method's description here.
 * Creation date: (4.2.2001 22:02:29)
 * @param group java.lang.String
 */
public boolean moveToGroup(java.lang.String group) {
	String currentParent = Group.substractParentName(recordData.getName());
	if (group.equalsIgnoreCase(currentParent)) return false;
	
	String oldName = getName();
	String newName;
	if (group.equals(nullString))
		newName = Group.substractObjectName(recordData.getName());
	else
		newName = group+Constants.GROUP_SEPARATOR+
				  Group.substractObjectName(recordData.getName());;

	// object with new name already exists, add suffix // !!!
	Object obj;
	while ((obj=Group.getRoot().findObject(newName, true))!=null)
	{
		if (obj==this)	// it's me :) already moved, fix data
		{
			recordData.setName(newName);
			fixLinks();
			return true;
		}
		else
			//return false;
			newName += Constants.MOVE_SUFFIX;
			return rename(newName);
	}
	
	getParent().removeObject(Group.substractObjectName(getName()));
	setParent(null);
	Group.getRoot().addSubObject(newName, this, true);

	//String oldGroup = Group.substractParentName(recordData.getName());
	recordData.setName(newName);
	/*//fixEPICSInLinks(recordData.getName(), newName);
	//fixEPICSOutLinks(oldGroup, group);			// only if target is moving !!!*/
	fixLinks();
	unconditionalValidation();

	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (21.12.2000 21:58:56)
 * @param g java.awt.Graphics
 * @param hilited boolean
 */
private void paintSubObjects(Graphics g, boolean hilited) {
	Enumeration e = subObjectsV.elements();
	VisibleObject vo;
	while (e.hasMoreElements()) {
		vo = (VisibleObject)(e.nextElement());
			vo.paint(g, hilited);
	}
	
}
/**
 * Insert the method's description here.
 * Creation date: (29.1.2001 22:40:48)
 * @param link com.cosylab.vdct.graphics.objects.Linkable
 */
public void removeLink(Linkable link) {
	if (getSubObjectsV().contains(link)) {
		Field field = (Field)link;
		removeObject(field.getFieldData().getName());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2.5.2001 23:23:32)
 * @param newName java.lang.String
 */
public boolean rename(java.lang.String newName) {
	
	String newObjName = Group.substractObjectName(newName);
	String oldObjName = Group.substractObjectName(getName());


	if (!oldObjName.equals(newObjName))
	{
		getParent().removeObject(oldObjName);
		String fullName = com.cosylab.vdct.util.StringUtils.replace(getName(), oldObjName, newObjName);
		getRecordData().setName(fullName);
		getParent().addSubObject(newObjName, this);

		// fix connectors IDs
		Enumeration e = subObjectsV.elements();
		Object obj; Connector connector;
		while (e.hasMoreElements()) {
			obj = e.nextElement();
			if (obj instanceof Connector)
			{
				connector = (Connector)obj;
				String id = connector.getID();
				id = com.cosylab.vdct.util.StringUtils.replace(id, oldObjName, newObjName);
				connector.setID(id);
			}
		}
	}
	
	// move if needed
	if (!moveToGroup(Group.substractParentName(newName)))
		fixLinks();			// fix needed

	return true;
	
}
/**
 * Insert the method's description here.
 * Creation date: (26.1.2001 17:18:51)
 */
private void revalidateFieldsPosition() {

  int nx, ny;
  ny = getY()+getHeight();
  Enumeration e = subObjectsV.elements();
  Field field; Object obj;
  while (e.hasMoreElements()) {
	obj = e.nextElement();
	if (obj instanceof Field) {
		field = (Field)obj;
		nx = getX()+(getWidth()-field.getWidth())/2;
		field.revalidatePosition(nx, ny);
		ny+=field.getHeight();
	}
  }

}
/**
 * Insert the method's description here.
 * Creation date: (21.12.2000 21:22:45)
 */
public void revalidatePosition() {
  setRx((int)(getX()*getRscale()));
  setRy((int)(getY()*getRscale()));

  // sub-components
  revalidateFieldsPosition();
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 16:58:58)
 */
public void rotate() { right=!right; }
/**
 * Insert the method's description here.
 * Creation date: (27.12.2000 12:45:23)
 * @return boolean
 */
public boolean selectAllComponents() {
	
	ViewState view = ViewState.getInstance();
	boolean anyNew = false;
	
	Enumeration e = subObjectsV.elements();
	VisibleObject vo;
	while (e.hasMoreElements()) {
		vo = (VisibleObject)(e.nextElement());
		if (vo instanceof Selectable)
			if (view.setAsSelected(vo)) anyNew = true;
	}

	return anyNew;
}
/**
 * Insert the method's description here.
 * Creation date: (24.4.2001 17:40:55)
 * @param description java.lang.String
 */
public void setDescription(java.lang.String description) {}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:53)
 * @param id java.lang.String
 */
public void setLayerID(java.lang.String id) {
	// not needed, id is retrieved dynamicaly via parent
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 11:47:54)
 * @param output com.cosylab.vdct.graphics.objects.OutLink
 * @param prevOutput com.cosylab.vdct.graphics.objects.OutLink
 */
public void setOutput(OutLink output, OutLink prevOutput) {
	if (prevOutput!=null) outlinks.removeElement(prevOutput);
	if (!outlinks.contains(output)) {
		outlinks.addElement(output);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 16:58:58)
 * @param state boolean
 */
public void setRight(boolean state) { right=state; }
/**
 * Insert the method's description here.
 * Creation date: (3.2.2001 13:25:42)
 * @param newTarget boolean
 */
public void setTarget(boolean newTarget) {
	target = newTarget;
}
/**
 * Insert the method's description here.
 * Creation date: (10.1.2001 14:49:50)
 * @return java.lang.String
 */
public String toString() {
	return recordData.toString();
	// recordData.getName()+" ("+recordData.getType()+")"
}
/**
 * Insert the method's description here.
 * Creation date: (21.12.2000 20:46:35)
 */
protected void validate() {

  double scale = getRscale();
  int rwidth = (int)(getWidth()*scale);
  int rheight = (int)(Constants.RECORD_HEIGHT*scale);
  setRheight(rheight);
  setRwidth(rwidth);

  // set appropriate font size
  int x0 = (int)(8*scale);		// insets
  int y0 = (int)(4*scale);

  Font font;
  setLabel(recordData.getName());
  if (rwidth<(2*x0)) font = null;
  else
	  font = FontMetricsBuffer.getInstance().getAppropriateFont(
		  			Constants.DEFAULT_FONT, Font.PLAIN, 
	 	 			getLabel(), rwidth-x0, (rheight-y0)/2);

  if (font!=null) {
	  FontMetrics fm = FontMetricsBuffer.getInstance().getFontMetrics(font);
	  setRlabelX((rwidth-fm.stringWidth(getLabel()))/2);
 	  setRlabelY(rheight/2+(rheight/2-fm.getHeight())/2+fm.getAscent());
  }
  setFont(font);
  
  label2 = recordData.getType();
  if (rwidth<(2*x0)) typeFont = null;
  else
	  typeFont = FontMetricsBuffer.getInstance().getAppropriateFont(
		  			 Constants.DEFAULT_FONT, Font.PLAIN, 
	 	 			 label2, rwidth-x0, (rheight-y0)/2);

  if (typeFont!=null) {
	  FontMetrics fm = FontMetricsBuffer.getInstance().getFontMetrics(typeFont);
	  rtypeLabelX = (rwidth-fm.stringWidth(label2))/2;
 	  rtypeLabelY = (rheight/2-fm.getHeight())/2+fm.getAscent();
  }

  if (rwidth<(2*x0)) fieldFont = null;
  else
	  fieldFont = FontMetricsBuffer.getInstance().getAppropriateFont(
		  			 Constants.DEFAULT_FONT, Font.PLAIN, 
	 	 			 fieldMaxStr, rwidth-x0, rheight-y0);

  int ascent = 0;
  rfieldRowHeight = 0;
  if (fieldFont!=null) {
	  FontMetrics fm = FontMetricsBuffer.getInstance().getFontMetrics(fieldFont);
	  rfieldLabelX = x0;
 	  rfieldLabelY = rheight+2*fm.getAscent();
	  rfieldRowHeight = fm.getHeight();
	  ascent = fm.getAscent();
  }

  rheight += y0+rfieldRowHeight*changedFields.size()+ascent;
  setRheight(rheight);
  setHeight((int)(rheight/scale));

  // sub-components
  revalidatePosition();		// rec's height can be different
  validateFields();
 
}
/**
 * Insert the method's description here.
 * Creation date: (26.1.2001 17:19:47)
 */
private void validateFields() {

	Enumeration e = subObjectsV.elements();
	Object obj;
	while (e.hasMoreElements()) {
		obj = e.nextElement();
		if (obj instanceof Field ||
			obj instanceof Connector)
			((VisibleObject)obj).validate();
	}
	
}
}