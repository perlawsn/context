package org.dei.perla.context;

import java.util.List;

import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.Statement;

public class Context {
	
	private final String name;
	private final List<ContextElement> contElements;
	private List<Statement> enable;
	private List<Statement> disable;
	private Refresh refresh;
	private boolean isActive;
	
	public Context(String name, List<ContextElement> contElements) {
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
	
	public void setActive(){
		isActive = true;
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
	

}
