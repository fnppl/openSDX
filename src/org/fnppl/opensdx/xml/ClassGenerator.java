package org.fnppl.opensdx.xml;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.fnppl.opensdx.common.BaseObject;
import org.fnppl.opensdx.common.Util;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class ClassGenerator {
	
	 public static void createBaseClassesFromXML(File xml, File header, File saveToPath) {
	    	try {
	    		if (!saveToPath.exists()) {
	    			saveToPath.mkdirs();
	    		}
	    		String head = null;
	    		if (header.exists()) head = Util.loadText(header.getAbsolutePath());
	    		Vector<String> classesReady = new Vector<String>();
	    		Document doc = new SAXBuilder().build(xml);
	    		
				Element e = doc.getRootElement();
				buildClass(e, head, saveToPath, classesReady);
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	    }
	    
	    private static void buildClass(Element e, String head, File saveToPath, Vector<String> classesReady) {
	    	String name = e.getName();
	    	name = Util.firstLetterUp(name);
	    	name = Util.cutNumericEnding(name);
	    	
	    	if (classesReady.contains(name)) return;
	    	
	    	StringBuffer b = new StringBuffer();
	    	StringBuffer m = new StringBuffer();
	    	
	    	if (head!=null) {
	    		b.append(head);
	    		b.append("\n");
	    	}
	    	b.append("import java.util.Vector;\n");
	    	b.append("import org.fnppl.opensdx.common.BaseObjectWithConstraints;\n");
	    	b.append("\n");
	    	b.append("public class "+name+" extends BaseObjectWithConstraints {\n");
	    	b.append("\n");
	    	//constructor
	    	b.append("	public "+name+"() {\n");
			Vector<String> vars = new Vector<String>();
	    	List<Element> vc = e.getChildren();
	    	
	    	//check comments for constraint type
	    	List l = e.getContent();
	    	HashMap<String, String> cnst = new HashMap<String,String>();
	    	String ename = null;
			for (Object i : l) {
				//System.out.println(i.getClass().getName());
				if (i instanceof Element) {
					ename = ((Element)i).getName();
				}
				if (i instanceof Comment) {
					if (ename!=null) {
						String comment = ((Comment)i).getText();
						if (comment.contains("MUST")) cnst.put(ename, "MUST");
						else if (comment.contains("SHOULD")) cnst.put(ename, "SHOULD");
						else if (comment.contains("COULD")) cnst.put(ename, "COULD");
						else cnst.put(ename, "?");
						//System.out.println(ename+" :: comment="+((Comment)i).getText());
					}
				}
			}
			
	    	for (Element c : vc)  {
	    		String n = c.getName();
	    		//System.out.println(c.toString());
	    		String constraintType = cnst.get(n);
	    		if (constraintType==null) constraintType = "[no comment]";
	    		
	    		boolean numeric = false;
	    		//check for numeric ending
				String cutN = Util.cutNumericEnding(n);
	        	if (!cutN.equals(n)) {
	        		n = cutN;
	        		numeric = true;
	        	}
	    		
	    		
	    		String vName = n;
	    		//check variable name
	    		if (n.equals("break")) vName = "sBreak";
	    		if (n.equals("for")) vName = "sFor";
	    		if (n.equals("if")) vName = "sIf";
	    		if (n.equals("else")) vName = "sElse";
	    		
	    		boolean hasAttributes = false;
	    		String args1 = "";
	    		String args2 = "";
	    		if (c.getAttributes().size()>0) {
	    			hasAttributes = true;
	    			for (String[] s : getAttributes(c)) {
	    				//System.out.println("   "+s[0]+"::"+s[1]);
	    				args1 += "String "+s[0]+", ";
	    				args2 += "\""+s[0]+"\", "+s[0]+", ";
	    			}
	    		}
	    		//one or vector
				int count = e.getChildren(n).size();
				if (numeric) {
					//some more effort needed
					List<Element> child = e.getChildren();
					count = 0;
					for (Element ec : child) {
						String cn = Util.cutNumericEnding(ec.getName());
						if (n.equals(cn)) {
							count++;
						}
					}
				}
				boolean add = false;
				String type = Util.firstLetterUp(n);
				
				//System.out.println(c.getName()+" has children: "+c.getChildren().size());
				if (c.getChildren().size()>0) {
	    			buildClass(c,head,saveToPath, classesReady);
	    		} else {
	    			type = "String";
	    		}
				
	    		if (count == 1 && !hasAttributes) {
		    		b.append("		names.add(\""+n+"\");");
					b.append(" values.add(null);");
					b.append(" constraints.add(\""+constraintType+"\");\n");
					if (constraintType.equals("[no comment]")) {
		    			System.out.println("WARNING: please check constraints for: "+name+"::"+n);
		    		}
	    		} else {
	    			if (!vars.contains(n)) {
	    				if (hasAttributes) {
	    					b.append("		names.add(\""+n+"\"); ");
		    				b.append(" values.add(new Vector<String[]>());");
		    				b.append(" constraints.add(\""+constraintType+"\");\n");
		    				vars.add(n);
	    					add = true;
	    				} else {
		    				b.append("		names.add(\""+n+"\");");
		    				b.append(" values.add(new Vector<"+type+">());");
		    				b.append(" constraints.add(\""+constraintType+"\");\n");
		    				vars.add(n);
		    				add = true;
	    				}
	    				if (constraintType.equals("[no comment]")) {
	    	    			System.out.println("WARNING: please check constraints for: "+name+"::"+n);
	    	    		}
	    			}
	    		}
	    		
//	    		if (name.equals("Contributors")) {
//					System.out.println(name+"::count = "+count);
//					System.out.println(name+"::hasAtt= "+hasAttributes);
//					System.out.println(name+"::add = "+add);
//				}
				if (count == 1) {
	    			//set and get methods
	    			m.append("	public void set"+Util.firstLetterUp(n)+"("+type+" "+vName+") {\n");
	    			m.append("		set(\""+n+"\", "+vName+");\n");
	    			m.append("	}\n");
	    			m.append("\n");
	    			if (type.equals("String")) {
	    				m.append("	public String get"+Util.firstLetterUp(n)+"() {\n");
	    				m.append("		return get(\""+n+"\");\n");
	    			} else {
	    				m.append("	public "+type+" get"+Util.firstLetterUp(n)+"() {\n");
	        			m.append("		return ("+type+")getObject(\""+n+"\");\n");
	    			}
	    			m.append("	}\n");
	    			m.append("\n");
				} else {
					if (add) {
						if (hasAttributes) {
							//get, add and remove methods
							m.append("	public Vector<String[]> get"+Util.firstLetterUp(n)+"() {\n");
			    			m.append("		return (Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"));\n");
			    			m.append("	}\n");
			    			m.append("\n");
							m.append("	public void add"+Util.firstLetterUp(n)+"("+args1+"String "+vName+") {\n");
		    				m.append("		((Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"))).add(new String[]{"+args2+vName+"});\n");
			    			m.append("	}\n");
			    			m.append("\n");
			    			m.append("	public void remove"+Util.firstLetterUp(n)+"(int index) {\n");
			    			m.append("		((Vector<String[]>)values.elementAt(names.indexOf(\""+n+"\"))).remove(index);\n");
			    			m.append("	}\n");
			    			m.append("\n");
						} else {
		    				//get, add and remove methods
							m.append("	public Vector<"+type+"> get"+Util.firstLetterUp(n)+"() {\n");
			    			m.append("		return (Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"));\n");
			    			m.append("	}\n");
							m.append("	public void add"+Util.firstLetterUp(n)+"("+type+" "+vName+") {\n");
		    				m.append("		((Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"))).add("+vName+");\n");
			    			m.append("	}\n");
			    			m.append("\n");
			    			m.append("	public void remove"+Util.firstLetterUp(n)+"("+type+" "+vName+") {\n");
			    			m.append("		((Vector<"+type+">)values.elementAt(names.indexOf(\""+n+"\"))).remove("+vName+");\n");
			    			m.append("	}\n");
			    			m.append("\n");
						}
					}
				}
	    		
	    	}
	    	b.append("	}\n"); // end of constructor
	    	b.append("\n");
	    	b.append("// methods\n");
	    	b.append(m);
	    	b.append("}\n");
	    	//System.out.println("Class: \t"+name);
	    	Util.saveTextToFile(b.toString(), saveToPath.getAbsolutePath()+"/"+name+".java");
	    	classesReady.add(name);
	    }
	    
	    private static Vector<String[]> getAttributes(Element base) {
			Vector<String[]> atts = new Vector<String[]>();
			List<Attribute> l = (List<Attribute>)base.getAttributes();
			if (l!=null) {
				for (Attribute a : l) {
					atts.add(new String[]{a.getName(), a.getValue()});
				}
			}
			return atts;
		}
	    
	    public static void main(String[] args) {
	    	File xml = new File("src/org/fnppl/opensdx/dmi/resources/example_feed.xml");
	    	File saveToPath = new File("src/org/fnppl/opensdx/commonAuto");
	    	
	    	File header = new File("header.txt");
	    	
	    	createBaseClassesFromXML(xml, header, saveToPath);	//if (1==1) return;
	    	
	    	try {
		    	BaseObject test = BaseObject.fromElement(org.fnppl.opensdx.xml.Document.fromFile(xml).getRootElement());
		    	System.out.println(test.getClass().getName());
		    	
		    	//Document.buildDocument(test.toElement()).output(System.out);
		    	
		    	//org.fnppl.opensdx.commonAuto.Feed feed = new org.fnppl.opensdx.commonAuto.Feed();
		    	//feed.addBundle(new org.fnppl.opensdx.commonAuto.Bundle());
		    	//feed.setItem("Test");
		    	//feed.setFeedinfo(new org.fnppl.opensdx.commonAuto.Feedinfo());

		    	//feed.checkConstraints(errorMsg);
		    	
		    	org.fnppl.opensdx.xml.Element errorMsg = ((org.fnppl.opensdx.commonAuto.Feed)test).checkConstraints();
		    	org.fnppl.opensdx.xml.Document.buildDocument(errorMsg).output(System.out);
		    	
		    	org.fnppl.opensdx.commonAuto.Feed feed = (org.fnppl.opensdx.commonAuto.Feed)test;
		    	
		    	//org.fnppl.opensdx.commonAuto.Feedinfo info = feed.getFeedinfo();
		    	//Document.buildDocument(info.toElement()).output(System.out);
		    	
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    	}
	    }
}
