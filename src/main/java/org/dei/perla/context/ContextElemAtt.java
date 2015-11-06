package org.dei.perla.context;

import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.query.expression.Expression;


public class ContextElemAtt implements ContextElement{
	
	private final String dimension;
	private final String attribute;
	private final ExpressionAST expAST;
	private Expression exp;
	private Object[] sample;
	
	private ContextElemAtt(String dimension, String attribute, ExpressionAST expAST){
		this.dimension = dimension;
		this.attribute = attribute;
		this.expAST = expAST;
	}

	public String getAttribute() {
		return attribute;
	}
	
	public String getDimension() {
		return dimension;
	}

	public ExpressionAST getExpressionAST() {
		return expAST;
	}
	
	public Object[] getSample() {
		return sample;
	}

	public void setSample(Object[] sample) {
		this.sample = sample;
	}
	
	public Expression getExpression() {
		return exp;
	}

	public void setExpression(Expression exp) {
		this.exp = exp;
	}

	public static ContextElement create(String dimension, String att, ExpressionAST ast, ParserContext ctx){
		if(ContextUtils.isDimensionAttValid(dimension, att)) 
			ctx.addError("DIMENSION " + dimension + " does not have the ATTRIBUTE " + att);
		return new ContextElemAtt(dimension, att, ast);
	}






}
