package org.dei.perla.cdt;

import java.io.StringReader;

import org.dei.perla.lang.parser.ParseException;
import org.dei.perla.lang.parser.ParserAST;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.StatementAST;



public class PartialComponent {

	private String enable;
	private StatementAST stat;
	
	public static PartialComponent EMPTY = new PartialComponent("void", null);
	
	private PartialComponent(String enable, StatementAST s){
		this.enable = enable;
		this.stat = s;
	}
	
	public static PartialComponent create(String enable, ParserContext ctx, String src){
		StatementAST ast = null;
        ParserAST p = new ParserAST(new StringReader(enable));
        try {
            ast = p.Statement(ctx);
        } catch(ParseException e) {
        	 ctx.addError("Syntax error in definying Partial Component of CONCEPT " + src);
        }
		return new PartialComponent(enable, ast);
	}
	
	public String getQuery(){
		return enable;
	}
	
	public StatementAST getStatement(){
		return stat;
	}

	public String toString(){
		return enable;
	}
    
	public class Comparator<RefreshAst>{
		
	}
	
}
