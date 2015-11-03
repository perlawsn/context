package org.dei.perla.cdt;

import org.dei.perla.lang.parser.ParserContext;

/* 
 * CREATE DIMENSION Città
 * CREATE ATTRIBUTE Nome 
 * EVALUATED ON PerlaQuery / static 
 */

public class CreateAttr {

	private final String name;
	private EvaluatedOn eo;
	private Object currentValue;
	
	//just for testing
	public CreateAttr(String name){
		this.name = name;
		eo = null;
	}
	
	public static CreateAttr EMPTY  = new CreateAttr(" ", null);
	
	private CreateAttr(String name, EvaluatedOn eo){
		this.name = name;
		this.eo = eo;
	}

	public String getName() {
		return name;
	}
	
	public EvaluatedOn getEvaluatedOn(){
		return eo;
	}

	public static CreateAttr create(String name, String evaluatedOn, ParserContext ctx, String src){
		EvaluatedOn e = QueryEvaluatedOn.create(evaluatedOn, ctx, src);
        return new CreateAttr(name, e);
	}

	public Object getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(Object current_value) {
		this.currentValue = current_value;
	}
	
	
}
