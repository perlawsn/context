package org.dei.perla.cdt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.Token;
import org.dei.perla.lang.parser.TypeVariable;
import org.dei.perla.lang.parser.ast.ConstantAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.query.expression.Constant;
import org.dei.perla.lang.query.expression.Expression;

/*
 * A function requested by the user
 * The class contains the complete path of the method (package.className.method)  
 */
public class MethodAST extends ExpressionAST{

	private String className;
	private String methodName;
	
	private MethodAST(Token token, String className, String methodName) {
		super(token);
		this.className = className;
		this.methodName = methodName;
	}
	
	private MethodAST(String className, String methodName) {
		super(null);
		this.className = className;
		this.methodName = methodName;
	}
	
	public static MethodAST create(Token token, String symbolicFunction, ParserContext ctx){
		String[] results = checkValidity(symbolicFunction, ctx);
		return new MethodAST(token, results[0], results[1]);
	}
	
	public static MethodAST create(String symbolicFunction, ParserContext ctx){
		String[] results = checkValidity(symbolicFunction, ctx);
		return new MethodAST(results[0], results[1]);
	}

    @Override
    protected void setType(TypeVariable type) {
        throw new IllegalStateException("Cannot set type to MethodAST node");
    }
	
	@Override
	protected boolean inferType(TypeVariable bound, ParserContext ctx) {
		return true;
	}

	@Override
	protected Expression toExpression(ParserContext ctx, AttributeOrder ord) {
		return MethodExp.create(className, methodName); 
	}

	private static String[] checkValidity(String methodName, ParserContext ctx) {
		String[] results = new String[2];
		int index = methodName.lastIndexOf( '.' );
		int len = methodName.length();
		String className = methodName.substring(0,index);
		String function = methodName.substring(index+1, len);
		Class classe = Class.class;
		try {
			classe = Class.forName(className);
			results[0] = className;
		} catch (ClassNotFoundException e) {
			ctx.addError(e.getMessage());
			results[0] = "";
		}
		Method getFunctionName;
		try {
			getFunctionName = classe.getDeclaredMethod(function, null);
			results[1] = (String) getFunctionName.invoke(classe.newInstance());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException |
				IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			ctx.addError(e.getMessage());
			results[1] = "";
		}
		return results;
	}
	
}
