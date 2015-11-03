package org.dei.perla.cdt.parser;

import java.io.StringReader;

import org.dei.perla.cdt.CDT;
import org.dei.perla.lang.parser.ParserContext;


public final class CDTreeParser {

    public CDT parser(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        CDT cdt;

        CDTParser p = new CDTParser(new StringReader(text));
        try {
            cdt = p.CDT(ctx);
        } catch(ParseException e) {
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());
        }

        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }

        return cdt;
    }
}
