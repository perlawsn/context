package org.dei.perla.context.parser;

import java.io.StringReader;
import java.util.List;

import org.dei.perla.cdt.CDTUtils;
import org.dei.perla.cdt.Concept;
import org.dei.perla.context.parser.ParseException;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextElemSimple;
import org.dei.perla.lang.parser.ParserContext;

import com.google.common.collect.Multimap;

public class ContextParser {

	public List<Context> create(String text) throws ParseException  {
        ParserContext ctx = new ParserContext();
        List<Context> contexts;
        ContParser p = new ContParser(new StringReader(text));
        try {
            contexts = p.CreateContexts(ctx);
        } catch(ParseException e) {
            throw new ParseException("Syntax error: " + ctx.getErrorDescription());
        }
        if (ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        for(Context c: contexts){
	        validUselessContext(c, ctx);
        } if(ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        return contexts;
    }
	
	public boolean validUselessContext(Context context, ParserContext ctx){
		int size = context.getContextElements().size();
		boolean valid = true;
		for (int i=0; i < size; i++) {
			if(context.getContextElements().get(i) instanceof ContextElemSimple){
				ContextElemSimple ces = (ContextElemSimple) context.getContextElements().get(i);
				String dimension = ces.getDimension();
				for(int j = i+1; j < size; j++){
					if(!(context.getContextElements().get(j) instanceof ContextElemSimple))
						continue;
					else{
						ContextElemSimple next = (ContextElemSimple) context.getContextElements().get(j);
						
						//check for mutually exclusive concept brothers
						if(dimension.equals(next.getDimension())){
								valid = false;
								ctx.addError("The ACTIVE IF contains a dimension " + dimension + 
										" that has more than one concept children");
								break;
						}
						
						//check for useless context constraints
						Concept concept = CDTUtils.getConcept(next.getDimension(), next.getValue());
						Multimap<String, String> map = concept.getConstraints();
						if(map.containsEntry(dimension, ces.getValue())) {
								ctx.addError("The context does not respect the useless context contraints. "
									+ "It is not possible to use " + next.getDimension() + " = " + next.getValue() +
									" with " + dimension + " = " + ces.getValue());
								valid = false;
								break;
							}
						}
					} if(!valid) break;
			} else 
				continue;
		}
		return valid;
	}
	
	public String removeContext(String text) throws ParseException {
		ContParser p = new ContParser(new StringReader(text));
		return p.RemoveContext();
    }
	
}
