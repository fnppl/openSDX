package org.fnppl.opensdx.common;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

import org.fnppl.opensdx.xml.Element;

public abstract class BaseObjectWithConstraints extends BaseObject {

	protected Vector<String> constraints = new Vector<String>();
	
	public String getConstraint(int index) {
		return constraints.get(index);
	}
	public Element checkConstraints() {
		Element output = new Element("constraints_check");
		checkConstraints(output);
		return output;
	}
	
	private void checkConstraints(Element output) {
		Element rootOfClass = new Element(getClass().getName().substring(getClass().getName().lastIndexOf(".")+1).toLowerCase());
		for (int i=0;i<names.size();i++) {
			if (i>constraints.size()-1) {
				System.out.println("ERROR in constraints settings at: "+getClass().getName()+" name: "+names.get(i));
			}
			//errors
			if (constraints.get(i).equals("MUST")) {
				if (values.get(i)==null) {
					rootOfClass.addContent("error","missing element::"+names.get(i));
				} else {
					if (values.get(i) instanceof Vector) {
						if (((Vector)values.get(i)).size()==0) {
							rootOfClass.addContent("error","missing element::"+names.get(i));
						} else {
							if (((Vector)values.get(i)).get(0) instanceof BaseObjectWithConstraints) {
								for (BaseObjectWithConstraints cown : (Vector<BaseObjectWithConstraints>)values.get(i)) {
									cown.checkConstraints(rootOfClass);
								}
							}
						}
					} else {
						if (values.get(i) instanceof BaseObjectWithConstraints) {
							((BaseObjectWithConstraints)values.get(i)).checkConstraints(rootOfClass);
						}
					}
				}
			}
			//warnings
			else if (constraints.get(i).equals("SHOULD")) {
				if (values.get(i)==null) {
					rootOfClass.addContent("warning","element should be present::"+names.get(i));
				} else {
					if (values.get(i) instanceof Vector) {
						if (((Vector)values.get(i)).size()==0) {
							rootOfClass.addContent("warning","element should be present::"+names.get(i));
						}  else {
							if (((Vector)values.get(i)).get(0) instanceof BaseObjectWithConstraints) {
								for (BaseObjectWithConstraints cown : (Vector<BaseObjectWithConstraints>)values.get(i)) {
									cown.checkConstraints(rootOfClass);
								}
							}
						}
					} else {
						if (values.get(i) instanceof BaseObjectWithConstraints) {
							((BaseObjectWithConstraints)values.get(i)).checkConstraints(rootOfClass);
						}
					}
				}
			} 
			//down the tree
			else if (values.get(i)!=null) {
				if (values.get(i) instanceof Vector) {
					if (((Vector)values.get(i)).size()>0) {
						if (((Vector)values.get(i)).get(0) instanceof BaseObjectWithConstraints) {
							for (BaseObjectWithConstraints cown : (Vector<BaseObjectWithConstraints>)values.get(i)) {
								cown.checkConstraints(rootOfClass);
							}
						}
					}
				} else {
					if (values.get(i) instanceof BaseObjectWithConstraints) {
						((BaseObjectWithConstraints)values.get(i)).checkConstraints(rootOfClass);
					}
				}
			}
		}
		//
		if (rootOfClass.getChildren().size()>0)
			output.addContent(rootOfClass);
	}
	
	/**
	 * overwrites set() from parent to ignore unknown class attributes 
	 * 
	 * @see org.fnppl.opensdx.common.BaseObject#set(java.lang.String, java.lang.Object)
	 */
	protected boolean set(String name, Object v) {
    	if(v==null) {
    		throw new RuntimeException("BaseObject::set("+name+") may not be null");
    	}
    	if (!names.contains(name)) {
    		//TODO	throw new RuntimeException("BaseObject::set("+name+") wrong attribute name");
    		System.out.println("IGNORING set("+name+") in "+this.getClass().getName()+" ::  wrong attribute name");
    		return false;
    	}
    	
    	if(changes == null) {
    		changes = new Hashtable<String, Object>();
    	}
        
        Object l = getObject(name);
        if(!v.equals(l)) {  
        	values.set(names.indexOf(name), v);
        	changes.put(name, v);
            return true;
        }
        
        return false;
    }
}
