package org.fnppl.opensdx.xml;

import java.util.Vector;

import org.fnppl.opensdx.common.BusinessObject;
import org.fnppl.opensdx.common.BusinessStringItem;


public abstract class ChildElementIterator {
	
	public ChildElementIterator(BusinessObject bo, String collectionName, String name) {
		if (bo==null) return;
		bo = bo.handleBusinessObject(collectionName);
		if (bo==null) return;
		Vector<XMLElementable> eChildren = bo.handleObjects(name);
		for (XMLElementable eChild : eChildren) {
			if (eChild instanceof BusinessObject) {
				processBusinessObject((BusinessObject)eChild);	
			}
			else if (eChild instanceof BusinessStringItem) {
				processBusinessStringItem((BusinessStringItem)eChild);	
			}
			else {
				processOther(eChild);
			}
		}
	}
	
	public ChildElementIterator(BusinessObject bo, String name) {
		if (bo==null) return;
		Vector<XMLElementable> eChildren = bo.handleObjects(name);
		for (XMLElementable eChild : eChildren) {
			if (eChild instanceof BusinessObject) {
				processBusinessObject((BusinessObject)eChild);	
			}
			else if (eChild instanceof BusinessStringItem) {
				processBusinessStringItem((BusinessStringItem)eChild);	
			}
			else {
				processOther(eChild);
			}
		}
	}
	
	public void processBusinessStringItem(BusinessStringItem item) {
		//hook in here
	}
	public void processBusinessObject(BusinessObject bo) {
		//hook in here
	}
	public void processOther(XMLElementable object) {
		//hook in here
	}
	
	
}
