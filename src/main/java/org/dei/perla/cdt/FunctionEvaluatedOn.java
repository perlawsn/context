package org.dei.perla.cdt;

import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.utility.Utility;


public class FunctionEvaluatedOn implements EvaluatedOn {
	
	private final String function;
	private final Object[] params;

	public FunctionEvaluatedOn(String function, Object[] params){
		this.function = function;
		this.params = params;
	}
	
	@Override
	public String getStringEvaluatedOn() {
		return function;
	}
	
	public Object[] params(){
		return params;
	}
	
	
	public Object computeValue() {
		Utility u = Utility.getInstance();
		Object result = null;
		if(!u.existsFunction(function)){
			System.out.println("The function " + function + " does not exists");
			return result;
		}
		else {
			result = u.retrieveValueFunction(function, null);
		}
		return result;
	}
	
	public static FunctionEvaluatedOn create(String attName, String evaluatedOn, ParserContext ctx, String src){
		if(evaluatedOn.contains("()")){
			int len = evaluatedOn.length()-2;
			evaluatedOn = evaluatedOn.substring(0, len);
		}
		Utility u = Utility.getInstance();
		if(!u.existsFunction(evaluatedOn)){
			ctx.addError("The function " + evaluatedOn + " does not exist");
		}
		else {
			Object result = u.retrieveValueFunction(evaluatedOn, null);
			if (result == null) 
				ctx.addError("The function " + evaluatedOn + " returned a null result");
		}
				//TODO aggiungere parametri
				return new FunctionEvaluatedOn(evaluatedOn, null);

	}

}
