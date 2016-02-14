package org.dei.perla.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.Statement;

public class Context extends Observable {
	
	private final String name;
	private final List<ContextElement> contElements;
	private List<Statement> enable;
	private List<Statement> disable;
	private Refresh refresh;
	private boolean isActive;
	
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	
	public Context(String name, List<ContextElement> contElements) {
		super();
		this.contElements = contElements;
		this.name = name;
		isActive = false;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ContextElement> getContextElements(){
		return contElements;
	}
	
	public boolean isActive(){
		return isActive;
	}
	
	public void setActive(boolean active){
		this.isActive = active;
		if(isActive)
			notifyObservers(this, enable);
		else
			notifyObservers(this, disable);
	}

	public List<Statement> getEnable() {
		return enable;
	}
	
	public void setEnable(List<Statement> enable) {
		this.enable = enable;
	}

	public List<Statement> getDisable() {
		return disable;
	}

	public void setDisable(List<Statement> disable) {
		this.disable = disable;
	}

	public Refresh getRefresh() {
		return refresh;
	}

	public void setRefresh(Refresh refresh) {
		this.refresh = refresh;
	}
	
	public ArrayList<Observer> getObservers() {
		return observers;
	}
	public void setObservers(ArrayList<Observer> observers) {
		this.observers = observers;
	}
	
	public void notifyObservers(Observable observable, List<Statement> blocks) {
		 for (Observer ob : observers) {
             ob.update(observable, blocks);
      }
	}

	public String toString(){
		StringBuilder elems = new StringBuilder("\nCONTEXT ELEMENTS:");
		for(ContextElement e: contElements){
			elems.append("\n");
			elems.append(e.toString());
		}
		return "CONTEXT " + name + elems;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!(o instanceof Context))
			return false;
		Context ctx = (Context) o; 
		//to do
		return true;
	}

}
