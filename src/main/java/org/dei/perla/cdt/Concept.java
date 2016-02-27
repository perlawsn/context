package org.dei.perla.cdt;

import java.util.Collections;
import java.util.List;

import org.dei.perla.lang.query.statement.Refresh;



public class Concept extends Node{
	
	private final List<CreateAttr> atts; 
	private final WhenCondition when;
	private final PartialComponent enable;
	private final PartialComponent disable;
	private final Refresh refresh;
	
	public static Concept ROOT = new Concept("ROOT", null, Collections.emptyList(), PartialComponent.EMPTY, 
			PartialComponent.EMPTY, Refresh.NEVER);
	
	public Concept(String name, WhenCondition when, List<CreateAttr> atts2, 
			PartialComponent enable, PartialComponent disable, Refresh refresh){
		super(name);
		this.when = when; 
		this.atts = atts2;
		this.enable = enable;
		this.disable = disable;
		this.refresh = refresh;
	}
	
	public List<CreateAttr> getAttributes() {
		return atts;		
	}
	
	public WhenCondition getWhen() {
		return when;
	}
	
	public PartialComponent getEnableComponent(){
		return enable;
	}

	public PartialComponent getDisableComponent(){
		return disable;
	}
	
	public Refresh getRefreshPeriod(){
		return refresh;
	}
	
	private int hasAttribute(String attribute){
		int index = 0;
		for(CreateAttr c: atts){
			if(c.getName().equals(attribute))
				return index;
			index++;
		}
		return -1;
	}
	
	public CreateAttr getAttribute(String name){
		int index = hasAttribute(name);
		if(index == -1)
			return CreateAttr.EMPTY;
		else 
			return atts.get(index);
	}
    

    public String toString(){
    	StringBuffer s = new StringBuffer("");
    	return "CONCEPT " + name;
    }
    
}
