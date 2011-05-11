package org.fnppl.opensdx.xml;

import java.util.Vector;

import org.fnppl.opensdx.common.BusinessObject;


public abstract class ChildElementIterator {

	
	public ChildElementIterator(Element e, String name, String childName) {
		if (e==null) return;
		Element element = e.getChild(name);
		if (element==null) return;
			
		Vector<Element> eChildren;
		if (childName==null) {
			eChildren = element.getChildren();
		} else {
			eChildren = element.getChildren(childName);
		}
		for (Element eChild : eChildren) {
			processChild(eChild);
		}
	}
	
	public ChildElementIterator(BusinessObject bo, String name, String childName) {
		if (bo==null) return;
		Element element = bo.handleElement(childName);
		if (element==null) return;
			
		Vector<Element> eChildren;
		if (childName==null) {
			eChildren = element.getChildren();
		} else {
			eChildren = element.getChildren(childName);
		}
		for (Element eChild : eChildren) {
			processChild(eChild);
		}
	}
	
	public abstract void processChild(Element child);
}
