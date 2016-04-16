package contextTest;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.ComposerManager;
import org.dei.perla.context.ConflictDetector;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextManager;
import org.dei.perla.context.IComposerManager;
import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.Plugin;
import org.dei.perla.core.channel.http.HttpChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.message.json.JsonMapperFactory;
import org.dei.perla.core.message.urlencoded.UrlEncodedMapperFactory;
import org.junit.Before;
import org.junit.Test;


public class CreateContextTest {
	
	public ContextManager ctxManager;
	private static final List<Plugin> plugins;
	  static {
	        List<Plugin> ps = new ArrayList<>();
	        ps.add(new HttpChannelPlugin());
	        ps.add(new SimulatorChannelPlugin());
	        plugins = Collections.unmodifiableList(ps);
	    }
	  PerLaSystem system = new PerLaSystem(plugins);

	public final String cdtString = new String(
			"CREATE DIMENSION mezzo_trasporto "
				+ "CREATE CONCEPT treno WHEN pressure:float > 10 WITH REFRESH COMPONENT: 5 h "
				+ "CREATE CONCEPT aereo WHEN pressure:float > 10 EXCLUDES tipo.manuale "
					+ " WITH REFRESH COMPONENT: 5 h  "
				+ "CREATE CONCEPT bus WHEN pressure:float > 10 "
					+ "EXCLUDES tipo.manuale WITH REFRESH COMPONENT: 5 h "
				+ "CREATE CONCEPT nave WHEN pressure:float > 10 WITH REFRESH COMPONENT: 5 h "
			+ "CREATE DIMENSION compagnia "
				+ "CREATE ATTRIBUTE $id_compagnia EVALUATED ON 'EVERY ONE SELECT id_compagnia:string " 
						+ " SAMPLING EVERY 5 d "
				 		+ " EXECUTE IF EXISTS(id_compagnia)' "
			+ "CREATE DIMENSION tipo "
			+ "CREATE CONCEPT manuale WHEN pressure:float > 10 EXCLUDES mezzo_trasporto.bus EXCLUDES mezzo_trasporto.aereo "
			+ "CREATE CONCEPT elettronico " 
					+ "CREATE ATTRIBUTE $id_apparecchio EVALUATED ON 'EVERY ONE SELECT id_apparecchio:string " 
						+ " SAMPLING EVERY 5 d "
				 		+ " EXECUTE IF EXISTS(id_apparecchio)' ");
	
	@Before
	public void createCtxManager() throws ParseException{
		ctxManager = new ContextManager(system, new ComposerManager(), new ConflictDetector());
		ctxManager.createCDT(cdtString);
	}
	
	@Test
	public void createContextTest() throws ParseException, org.dei.perla.context.parser.ParseException{
		String text = "CREATE CONTEXT biglietti_Italo_1 "
		 		+ "ACTIVE IF mezzo_trasporto = treno AND tipo = manuale";
		ctxManager.createContexts(text);
		IComposerManager compose = ctxManager.getComposerManager();
		Context biglietti_Italo_1 = compose.getPossibleContext("biglietti_Italo_1");
		ctxManager.startDetectingContext(biglietti_Italo_1);
		assertTrue(compose.getPossibleContexts().isEmpty());
		assertTrue(ctxManager.getContexts().size()==1);
	}

	@Test
	public void createContext() throws org.dei.perla.context.parser.ParseException {
		String text = "CREATE CONTEXT biglietti_CostaCrociere "
		 		+ "ACTIVE IF mezzo_trasporto = nave AND tipo = manuale";
		ctxManager.createContexts(text);
		IComposerManager compose = ctxManager.getComposerManager();
		Context biglietti_CostaCrociere = compose.getPossibleContext("biglietti_CostaCrociere");
		assertTrue(biglietti_CostaCrociere.getName().equals("biglietti_CostaCrociere"));
		ctxManager.startDetectingContext(biglietti_CostaCrociere);
		assertTrue(compose.getPossibleContexts().isEmpty());
		assertTrue(ctxManager.getContexts().size()==1);
	}

	@Test
	public void removeContext() throws org.dei.perla.context.parser.ParseException{
		String ctxItalo2 = "CREATE CONTEXT biglietti_Italo_2 "
		 		+ "ACTIVE IF mezzo_trasporto = nave AND tipo = manuale";
		ctxManager.createContexts(ctxItalo2);
		String text = "DROP CONTEXT biglietti_Italo_2";
		IComposerManager compose = ctxManager.getComposerManager();
		Context biglietti_Italo_2 = compose.getPossibleContext("biglietti_Italo_2");
		ctxManager.startDetectingContext(biglietti_Italo_2);
		assertTrue(ctxManager.getContexts().size() == 1);
		ctxManager.removeContext(text);
		assertNull(ctxManager.getContext("biglietti_Italo_2"));
		assertTrue(ctxManager.getContexts().size() == 0);
	}
	
	@Test(expected= org.dei.perla.context.parser.ParseException.class)
	public void brotherConcepts() throws org.dei.perla.context.parser.ParseException {
		String text = "CREATE CONTEXT biglietti_CostaCrociere "
		 		+ "ACTIVE IF mezzo_trasporto = nave AND mezzo_trasporto = aereo";
		ctxManager.createContexts(text);
	}
	
	@Test(expected= org.dei.perla.context.parser.ParseException.class)
	public void uselessContraints() throws org.dei.perla.context.parser.ParseException {
		String text = "CREATE CONTEXT biglietti_CostaCrociere "
		 		+ "ACTIVE IF mezzo_trasporto = aereo AND tipo = manuale";
		ctxManager.createContexts(text);
	}
	
	
}
