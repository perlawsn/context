package org.dei.perla.cdt;

import java.util.Collections;
import java.util.List;


public class Dimension extends Node{
	
	private final String father;
	private final CreateAttr att;
	private List<Concept> concepts;

	
	public Dimension(String name, String father, CreateAttr att){
		super(name);
		this.father = father;
		this.att = att;
		this.concepts = Collections.emptyList();
	}
	
	public Dimension(String name, String father, List<Concept> concepts){
		super(name);
		this.father = father;
		this.att = CreateAttr.EMPTY;
		this.concepts = concepts;
	}

	public String getFather(){
		return father;
	}
	
	public CreateAttr getAttribute() {
		return att;
	}

	public List<Concept> getConcepts() {
		return concepts;
	}
	
	public void addConcept(Concept child){
		concepts.add(child);
	}
	
	public void removeConcept(Concept child){
		concepts.add(child);
	}
	
	public boolean containsConcept(Concept c){
		if(c != null) 
			return containsConcept(c.getName());
		else
			return false;
	}
	
	public boolean containsConcept(String conceptName){
		for(Concept c: concepts){
			if(c.getName().equals(conceptName))
				return true; 
		}
		return false;
	}
	
	public void removeConceptByName(String conceptName){
		int index = findIndexConcept(conceptName);
		if(index >= 0)
			concepts.remove(index);
		else
			System.out.println("The dimension has not the concept " + conceptName);
	}
	
	public Concept getConceptContainingAtt(String attribute){
		for(Concept c: concepts){
			if(c.getAttribute(attribute)!= CreateAttr.EMPTY)
				return c;
		}
		return null;
	}
	
	private int findIndexConcept(String conceptName){
		for(int i=0; i < concepts.size(); i++){
			if(concepts.get(i).getName().equals(conceptName))
				return i;
		}
		return -1;
	}
	
	public String toString(){
		String children = new String();
		if(att != CreateAttr.EMPTY) {
			children = "\n" + att.toString();
		} else {
			StringBuffer s = new StringBuffer("\n [");
			for(int i=0; i < concepts.size() - 1; i++){
				s.append(concepts.get(i).getName());
				s.append(", ");
			}
			int lastElement = concepts.size()-1;
			s.append(concepts.get(lastElement).getName());
			s.append(" ]");
			children = s.toString();
		}
		return "DIMENSION " + name + " CHILD OF " + father + children;
	}


}
