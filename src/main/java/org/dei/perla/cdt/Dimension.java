package org.dei.perla.cdt;

import java.util.Collections;
import java.util.List;


public class Dimension extends Node{
	
	private final String father;
	private final CreateAttr att;
	private final List<Concept> concepts;
	private Concept corrValue;
	private String correntValue;
	
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
	
	public String toString(){
		String children = new String();
		if(att != null) {
			children = att.toString();
		} else {
			StringBuffer s = new StringBuffer("\n");
			for(Concept c: concepts) {
				s.append("\t");
				s.append(c.toString());
				s.append("\n");
			}
			children = s.toString();
		}
		return "DIMENSION " + name + " CHILD OF " + father + children;
	}

	public Concept getCorrValue() {
		return corrValue;
	}

	public String getCorrentValue() {
		return correntValue;
	}

	public void setCorrentValue(String correntValue) {
		Concept c = CDTUtils.getConcept(name, correntValue);
		if(!concepts.contains(c))
			throw new RuntimeException(
                    "Dimension " + name + " has not the Concept " + correntValue);
		this.correntValue = correntValue;
	}
	
	/*
	 * setta il valore corrente di una domensione
	 */
	public void setCorrValue(Concept correntValue) {
		if(!concepts.contains(correntValue))
			throw new RuntimeException(
                    "Dimension " + name + " has not the Concept " + correntValue.getName() );
		this.corrValue = correntValue;
	}

}
