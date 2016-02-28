package contextTest;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dei.perla.cdt.*;
import org.dei.perla.context.*;
import org.dei.perla.context.parser.ContParser;
import org.dei.perla.context.parser.ParseException;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.query.statement.Refresh;
import org.junit.Before;
import org.junit.Test;

public class ContextElementTest {
	
	private static CDT cdt = CDT.getCDT();
	
	@Before
	public void createCDT(){
	 Concept treno = new Concept("treno", null, Collections.emptyList(), null, null, Refresh.NEVER, Collections.emptyMap());
	 Concept aereo = new Concept("aereo", null, Collections.emptyList(), null, null, Refresh.NEVER, Collections.emptyMap() );
	 Concept bus = new Concept("bus", null, Collections.emptyList(), null, null, Refresh.NEVER, Collections.emptyMap() );
	 Concept nave = new Concept("nave", null, Collections.emptyList(), null, null, Refresh.NEVER, Collections.emptyMap() );
	 List<Concept> concTrasporto = Arrays.asList(new Concept[]{treno, aereo, bus, nave});
	 Dimension mezzo_trasporto = new Dimension("mezzo_trasporto", "ROOT", concTrasporto);
	
	 Dimension compagnia = new Dimension("compagnia", "ROOT", 
			new CreateAttr("id_compagnia", new FunctionEvaluatedOn("contextTest.TestClass", "getIdCompagnia")));
	
	 Concept manuale = new Concept("manuale", null, Collections.emptyList(), null, null, Refresh.NEVER, Collections.emptyMap());
	 CreateAttr ca = new CreateAttr("id_apparecchio", new FunctionEvaluatedOn("contextTest.TestClass", "getIdApparecchio"));
	 Concept elettrico = new Concept("elettrico", null, 
			Arrays.asList(new CreateAttr[]{ca}), null, null, Refresh.NEVER, Collections.emptyMap());
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
		parser.CreateContext(ctx);
        assertTrue(ctx.hasErrors());
	}
	
	@Test
	public void conceptBelongsToDimTest() throws ParseException {  
		ParserContext ctx = new ParserContext();
		ContParser parser = new ContParser(
				 new StringReader("CREATE CONTEXT biglietti_Italo_1 "
				 		+ "ACTIVE IF mezzo_trasporto = treno "));
		Context context = parser.CreateContext(ctx);
	    List<ContextElement> ce = context.getContextElements();
        assertFalse(ce.isEmpty());
        assertFalse(ctx.hasErrors());
	}
	
	// dimension.attributo = 'stringa'
	@Test
	public void DimAttributeTest() throws ParseException {  
		ParserContext ctx = new ParserContext();
		ContParser parser = new ContParser(
				 new StringReader("CREATE CONTEXT biglietti_Alitalia "
				 		+ "ACTIVE IF tipo.id_apparecchio = 'xx' AND compagnia.id_compagnia = 'Italo_1' "
				 		+ "AND mezzo_trasporto = aereo "));
		Context context = parser.CreateContext(ctx);
		assertFalse(ctx.hasErrors());
	    List<ContextElement> ce = context.getContextElements();
        assertTrue(ce.size() == 3 );
        ContextElement ca = ce.get(0);
        assertTrue(ca instanceof ContextElemAtt);
        ContextElemAtt cea0 = (ContextElemAtt) ca;
        assertThat(cea0.getDimension(), equalTo("tipo"));
        assertThat(cea0.getAttribute(), equalTo("id_apparecchio"));
        
        ca = (ContextElemAtt) ce.get(1);
        assertTrue(ca instanceof ContextElemAtt);
        ContextElemAtt cea1 = (ContextElemAtt) ca;
        assertThat(cea1.getDimension(), equalTo("compagnia"));
        assertThat(cea1.getAttribute(), equalTo("id_compagnia"));
        
        ca = (ContextElemSimple) ce.get(2);
        assertTrue(ca instanceof ContextElemSimple);
        ContextElemSimple ces = (ContextElemSimple) ca;
        assertThat(ces.getDimension(), equalTo("mezzo_trasporto"));
        assertThat(ces.getValue(), equalTo("aereo"));
        
	}


	
	
}
