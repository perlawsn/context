package org.dei.perla.context;

import org.dei.perla.cdt.CDTUtils;
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
		if(!CDTUtils.isDimensionConceptValid(dimension, value))
			ctx.addError("DIMENSION " + dimension + " does not have the CONCEPT " + value);
		return new ContextElemSimple(dimension, value);
	}
	
	public String toString(){
		return dimension + " = " + value;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!(o instanceof ContextElemSimple))
			return false;
		ContextElemSimple ces = (ContextElemSimple) o;
		return dimension.equals(ces.getDimension()) && value.equals(ces.getValue());
	}
	
}
