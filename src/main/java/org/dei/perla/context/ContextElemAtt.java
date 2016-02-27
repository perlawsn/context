package org.dei.perla.context;

import org.dei.perla.cdt.CDTUtils;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.query.expression.Expression;


public class ContextElemAtt implements ContextElement{
	

	private final String dimension;  
	private final String attribute;	
	private Expression exp;
	
	private ContextElemAtt(String dimension, String attribute, Expression exp){
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
	
	public Expression getExpression() {
		return exp;
	}

	public static ContextElement create(String concept, String att, ExpressionAST ast, ParserContext ctx){
		if(CDTUtils.isDimensionAttValid(concept, att)) 
			ctx.addError("CONCEPT " + concept + " does not have the ATTRIBUTE " + att);
		ParserContext ctx2 = new ParserContext();
		Expression e = ast.compile(DataType.ANY, ctx2, new AttributeOrder());
		if(ctx2.hasErrors()) 
			ctx.addError(ctx2.getErrorDescription());
		return new ContextElemAtt(concept, att, e);
	}

	public String toString(){
		return dimension + "." + attribute + " " + exp;
	}

	@Override
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!(o instanceof ContextElemAtt))
			return false;
		ContextElemAtt ces = (ContextElemAtt) o; 
		//TODO bisognerebbe implementare equals anche per le espressioni
		return dimension.equals(ces.getDimension()) && attribute.equals(ces.getAttribute());
	}


}
