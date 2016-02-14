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

	public Context create(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        Context context;
        ContParser p = new ContParser(new StringReader(text));
        try {
            context = p.CreateContext(ctx);
        } catch(ParseException e) {
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());
        }
        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        return context;
    }
	
	public String removeContext(String text) throws ParseException {
		ContParser p = new ContParser(new StringReader(text));
		return p.RemoveContext();
    }
	
}
