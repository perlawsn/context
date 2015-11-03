import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dei.perla.cdt.*;
import org.dei.perla.context.*;
import org.dei.perla.context.parser.ContParser;
import org.dei.perla.context.parser.ParseException;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.ComparisonAST;
import org.dei.perla.lang.query.statement.Refresh;
import org.junit.Before;
import org.junit.Test;


public class ContextElementTest {

	 CDT cdt = CDT.getCDT();
	
	@Before
	public void createCDT(){
	 Concept treno = new Concept("treno", null, Collections.emptyList(), null, null, Refresh.NEVER );
	 Concept aereo = new Concept("aereo", null, Collections.emptyList(), null, null, Refresh.NEVER );
	 Concept bus = new Concept("bus", null, Collections.emptyList(), null, null, Refresh.NEVER );
	 Concept nave = new Concept("nave", null, Collections.emptyList(), null, null, Refresh.NEVER );
	 List<Concept> concTrasporto = Arrays.asList(new Concept[]{treno, aereo, bus, nave});
	 Dimension mezzo_trasporto = new Dimension("mezzo_trasporto", "ROOT", concTrasporto);
	
	 Dimension compagnia = new Dimension("compagnia", "ROOT", 
			new CreateAttr("id_compagnia"));
	
	 Concept manuale = new Concept("manuale", null, Collections.emptyList(), null, null, Refresh.NEVER);
	 CreateAttr ca = new CreateAttr("id_apparecchio");
	 Concept elettrico = new Concept("elettrico", null, 
			Arrays.asList(new CreateAttr[]{ca}), null, null, Refresh.NEVER);
	 Dimension tipo = new Dimension("tipo", "ROOT", Arrays.asList(new Concept[]{manuale, elettrico}));
	 cdt.setDimensions(Arrays.asList(new Dimension[]{mezzo_trasporto, compagnia, tipo}));
	}
	
	
	@Test
	public void conceptDoesNotBelongToDimTest() throws ParseException {  
		ParserContext ctx = new ParserContext();
		ContParser parser = new ContParser(
				 new StringReader("CREATE CONTEXT biglietti_Italo_1 "
				 		+ "ACTIVE IF compagnia = Italo AND mezzo_trasporto = treno "
				 		+ ""));
		Context context = parser.Context(ctx);
	    List<ContextElement> ce = context.getContextElements();
        assertFalse(ce.isEmpty());
        assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void conceptBelongsToDimTest() throws ParseException {  
		ParserContext ctx = new ParserContext();
		ContParser parser = new ContParser(
				 new StringReader("CREATE CONTEXT biglietti_Italo_1 "
				 		+ "ACTIVE IF mezzo_trasporto = treno "
				 		+ ""));
		Context context = parser.Context(ctx);
	    List<ContextElement> ce = context.getContextElements();
        assertFalse(ce.isEmpty());
        assertFalse(ctx.hasErrors());
	}
	
	@Test
	public void DimAttributeTest() throws ParseException {  
		ParserContext ctx = new ParserContext();
		ContParser parser = new ContParser(
				 new StringReader("CREATE CONTEXT biglietti_Italo_1 "
				 		+ "ACTIVE IF tipo.id_apparecchio = xx "
				 		+ ""));
		Context context = parser.Context(ctx);
	    List<ContextElement> ce = context.getContextElements();
        assertFalse(ce.isEmpty());
        ContextElemAtt ca = (ContextElemAtt) ce.get(0);
        assertThat(ca.getAttribute(), equalTo("id_apparecchio"));
        assertFalse(ctx.hasErrors());
        assertTrue(ca.getExpression() instanceof ComparisonAST);
	}


	
	
}
