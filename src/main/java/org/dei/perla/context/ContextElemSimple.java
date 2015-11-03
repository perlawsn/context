package org.dei.perla.context;

import org.dei.perla.lang.parser.ParserContext;

public class ContextElemSimple implements ContextElement{
	
	private final String dimension;
	private final String value;
	
	private ContextElemSimple(String dimension, String value){
		this.dimension = dimension;
		this.value = value;
	}

	public String getDimension() {
		return dimension;
	}

	public String getValue() {
		return value;
	}
	
	public static ContextElement create(String dimension, String value, ParserContext ctx){
		if(!ContextUtils.isDimensionConceptValid(dimension, value))
			ctx.addError("DIMENSION " + dimension + " does not have the CONCEPT CHILD " + value);
		return new ContextElemSimple(dimension, value);
	}
	
	
	
}
