package org.dei.perla.context;

import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.ExpressionAST;


public class ContextElemAtt implements ContextElement{
	
	private final String dimension;
	private final String attribute;
	private final ExpressionAST exp;
	
	private ContextElemAtt(String dimension, String attribute, ExpressionAST exp){
		this.dimension = dimension;
		this.attribute = attribute;
		this.exp = exp;
	}

	public String getAttribute() {
		return attribute;
	}
	
	public String getDimension() {
		return dimension;
	}

	public ExpressionAST getExpression() {
		return exp;
	}

	public static ContextElement create(String dimension, String att, ExpressionAST ast, ParserContext ctx){
		if(ContextUtils.isDimensionAttValid(dimension, att)) 
			ctx.addError("DIMENSION " + dimension + " does not have the ATTRIBUTE " + att);
		return new ContextElemAtt(dimension, att, ast);
	}


}
