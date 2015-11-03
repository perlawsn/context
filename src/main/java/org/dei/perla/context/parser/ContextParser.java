package org.dei.perla.context.parser;

import java.io.StringReader;

import org.dei.perla.context.parser.ParseException;
import org.dei.perla.context.Context;
import org.dei.perla.lang.parser.ParserContext;

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
        return context;
    }
}
