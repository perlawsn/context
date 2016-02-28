package org.dei.perla.context.parser;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.dei.perla.cdt.CDTUtils;
import org.dei.perla.cdt.Concept;
import org.dei.perla.context.parser.ParseException;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextElemSimple;
import org.dei.perla.lang.parser.ParserContext;

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
        validUselessContext(context, ctx);
        if(ctx.getErrorCount() > 0) {
            throw new ParseException(ctx.getErrorDescription());
        }
        return context;
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
						Map<String, List<String>> map = concept.getConstraints();
						boolean hasDimensionConstraint = (map.get(dimension) != null) ? true : false;
						if(hasDimensionConstraint) {
							List<String> concepts = map.get(dimension);
							if(concepts.contains(ces.getValue())) {
								ctx.addError("The context does not respect the useless context contraints. "
									+ "It is not possible to use " + next.getDimension() + " = " + next.getValue() +
									" with " + dimension + " = " + ces.getValue());
								valid = false;
								break;
							}
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
