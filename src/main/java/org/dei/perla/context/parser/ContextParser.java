package org.dei.perla.context.parser;

import java.io.StringReader;

import org.dei.perla.context.parser.ParseException;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextElemAtt;
import org.dei.perla.context.ContextElement;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.AttributeOrder;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.query.expression.Expression;

public class ContextParser {

	public Context parser(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        Context context;
        ContParser p = new ContParser(new StringReader(text));
        try {
            context = p.Context(ctx);
        } catch(ParseException e) {
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());
        }
        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        for(ContextElement c: context.getContextElements()){
        	if(c instanceof ContextElemAtt){
        		ContextElemAtt elem = (ContextElemAtt) c;
        		ParserContext ctx1 = new ParserContext();
        		Expression exp = elem.getExpressionAST().compile(DataType.ANY, ctx1, new AttributeOrder());
                if(ctx.hasErrors()){
                	throw new ParseException(ctx1.getErrorDescription());
                }
                else
                	elem.setExpression(exp);
        	}
        }
        return context;
    }
}
