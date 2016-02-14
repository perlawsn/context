package org.dei.perla.cdt.parser;

import java.io.StringReader;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.lang.parser.ParserContext;


public final class CDTreeParser {

    public CDT parseCDT(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        CDT cdt;

        CDTParser p = new CDTParser(new StringReader(text));
        try {
            cdt = p.CDT(ctx);
        } catch(ParseException e) {
        	e.printStackTrace();
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());    
        }
        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        return cdt;
    }
    
    public Dimension parseAddDimension(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        CDTParser p = new CDTParser(new StringReader(text));
        Dimension dim;
        try {
            dim = p.CreateDimension(ctx);
        } catch(ParseException e) {
        	e.printStackTrace();
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());    
        }
        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        return dim;
    }
    
    public String parseRemoveDimension(String text) throws ParseException  {
        CDTParser p = new CDTParser(new StringReader(text));
        String dimToRemove = null;
        try {
        	dimToRemove = p.RemoveDimension();
        } catch(ParseException e) {
        	e.printStackTrace();   
        }
        return dimToRemove;
    }
}
