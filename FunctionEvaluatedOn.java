package org.dei.perla.cdt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dei.perla.lang.parser.ParserContext;

public class FunctionEvaluatedOn implements EvaluatedOn {
	
	private final String className;
	private final String methodName;

	public FunctionEvaluatedOn(String className, String methodName){
		this.className = className;
		this.methodName = methodName;
	}
	
	@Override
	public String getStringEvaluatedOn() {
		return className + "." + methodName;
	}
	
	public String getClassName(){
		return className;
	}
	
	public String getMethodName(){
		return methodName;
	}
	
	public Object computeValue() {
		Object result = null;
		Class classe = Class.class;
		try {
			classe = Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
		Method getFunctionName;
		try {
			getFunctionName = classe.getDeclaredMethod(methodName, null);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
		try {
			result = getFunctionName.invoke(classe.newInstance());
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static FunctionEvaluatedOn create(String attName, String evaluatedOn, ParserContext ctx, String src){
		int index = evaluatedOn.lastIndexOf( '.' );
		int len = evaluatedOn.length();
		String nameClass = evaluatedOn.substring(0,index);
		String nameMethod = evaluatedOn.substring(index+1, len);
		Class classe = Class.class;
		try {
			classe = Class.forName(nameClass);	
		} catch (ClassNotFoundException e) {
			ctx.addError("Exception in defining the EVALUATED ON clause "
            		+ "of ATTRIBUTE " + attName + " OF " + src + "\n" + e.getMessage());
			nameClass = "";
		}
		Method getNameFunction;
		try {
			getNameFunction = classe.getDeclaredMethod(nameMethod, null);
		} catch (NoSuchMethodException | SecurityException e) {
			ctx.addError("Exception in defining the EVALUATED ON clause "
            		+ "of ATTRIBUTE " + attName + " OF " + src + "\n" + e.getMessage());
			nameMethod = "";
		}
		return new FunctionEvaluatedOn(nameClass, nameMethod);
	}

}
