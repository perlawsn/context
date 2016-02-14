package org.dei.perla.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.FunctionEvaluatedOn;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.cdt.parser.CDTreeParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.parser.ContextParser;
import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.Plugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.lang.Executor;
import org.dei.perla.lang.StatementHandler;
import org.dei.perla.lang.executor.QueryException;
import org.dei.perla.lang.executor.Record;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.expression.LogicValue;
import org.dei.perla.lang.query.statement.Statement;


public class ContextManager {
	
	private final CDTreeParser cdtParser;
	private CDT cdt;
	private final ContextParser ctxParser;
	
	private List<Context> contexts;
	
	private final IComposerManager composerMgr;
	private List<RefreshContext> refreshContext;
	private final ContextExecutor ctxExecutor;
	
	private final List<Plugin> plugins;
    private final PerLaSystem system;
    private final Executor queryExec;

	//cache for quickly retrieving the current value of a dimension
	private static Map<String, Object> cache;
	
	private Map<String, Integer> cdtHandlerUtils;
	private List<StatementHandler> cdtHandlers;
	
	public ContextManager(IComposerManager composerMgr, IConflictDetector conflictDetector) {
		cdt = null;
		cdtParser = new CDTreeParser();
		ctxParser = new ContextParser();
		contexts = new ArrayList<Context>();
		refreshContext = new ArrayList<RefreshContext>();
		ctxExecutor = new ContextExecutor(conflictDetector);
		this.composerMgr = composerMgr;
		cache = new ConcurrentHashMap<String, Object>();
		plugins = Arrays.asList(new SimulatorMapperFactory(), new SimulatorChannelPlugin());
		system = new PerLaSystem(plugins);
		queryExec = new Executor(system);
		cdtHandlerUtils = new HashMap<String, Integer>();
		cdtHandlers = new ArrayList<>();
	}
	
	public CDT getCDT(){
		return cdt;
	}
	
	public IComposerManager getComposerManager(){
		return composerMgr;
	}
	
	public List<Context> getContexts() {
		return contexts;
	}
	
	public Context getContext(String ctxName){
		for(Context c: contexts){
			if(c.getName().equals(ctxName))
				return c;
		}
		return null;
	}
	
	private int getIndexContext(String ctxName){
		for(int i=0; i<contexts.size(); i++){
			if(contexts.get(i).getName().equals(ctxName))
				return i;
		}
		return -1;
	}
	
	public Map<String, Object> getCache(){
		Map<String, Object> shallowCopy = new HashMap<String, Object>();
		shallowCopy.putAll(cache);
		return shallowCopy;
	}
	
	public void createCDT(String text) throws ParseException{
		cdt = cdtParser.parseCDT(text);
		initCache(cdt);
	}
	
	public void addDimension(String text){
		if(cdt==null)
			throw new RuntimeException("Before adding a DIMENSION, it is necessary to create the CDT");
		Dimension dim;
		try {
			dim = cdtParser.parseAddDimension(text);
			cdt.addDimension(dim);
			for(Concept c: dim.getConcepts()){
				// FINIRE DI FARE AGGIORNANDO LA CACHE E GLI HANDLERS E TESTARE
			}
		} catch (ParseException e) {
			System.out.println("ERROR during the parsing of the request");
			e.printStackTrace();
		}
	}

	public void removeDimension(String text){
		if(cdt==null)
			throw new RuntimeException("Before removing a DIMENSION, it is necessary to create the CDT");
		String dim;
		try {
			dim = cdtParser.parseRemoveDimension(text);
			if(cdt.removeDimensionByName(dim)) {
				// FINIRE DI FARE AGGIORNANDO LA CACHE E GLI HANDLERS E TESTARE
			}
		} catch (ParseException e) {
			System.out.println("ERROR during the parsing of the request");
			e.printStackTrace();
		}
	}
	
	public void createContext(String text) throws org.dei.perla.context.parser.ParseException {
		if(cdt==null)
			throw new RuntimeException("Before creating a context it is necessary to create the CDT");
		Context ctx = ctxParser.create(text);
		composerMgr.addPossibleContext(ctx);
	}

	private void initCache(CDT cdt){
		int i = 0;
		for(Dimension d: cdt.getDimensions()){
			helper(d, i);
		}
	}
	
	private void helper(Dimension d, int i){
		String dimAttName;
		String dimConceptName;
		cache.put(d.getName(), new Object());
		if(d.getAttribute() != CreateAttr.EMPTY){
			CreateAttr att = d.getAttribute();
			dimAttName = concatenateWithDot(d.getName(), att.getName());
			cache.put(dimAttName, new Object());
			cdtHandlerUtils.put(dimAttName, i);
			cdtHandlers.add(new ContextElemAttHandler(d.getName(), att.getName()));
			i++;
		}
		else {
			for(Concept c: d.getConcepts() ){
				dimConceptName = concatenateWithDot(d.getName(), c.getName());
				cdtHandlerUtils.put(dimConceptName, i);
				cdtHandlers.add(new ContextElemSimpleHandler(d.getName(), c));
				i++;
				for(CreateAttr attr: c.getAttributes()){
					dimAttName = concatenateWithDot(d.getName(), attr.getName());
					cache.put(dimAttName, new Object());
					cdtHandlerUtils.put(dimAttName, i);
					cdtHandlers.add(new ContextElemAttHandler(d.getName(), attr.getName()));
					i++;
				}
			}
		}
	}
	
	private String concatenateWithDot(String a, String b){
		return a + "." + b; 
	}
	
	/*
	 * @param ctxName is the name of a previuosly declared context. 
	 */
	public void startDetectingContext(String ctxName){
		Context toDetect = composerMgr.getPossibleContext(ctxName);
		if(toDetect != null) {
			contexts.add(toDetect);
			toDetect.addObserver(ctxExecutor);
			composerMgr.removePossibleContext(toDetect);
			RefreshContext r = new RefreshContext(toDetect, cache);
			refreshContext.add(r);
			r.start();
		}
		else {
			System.out.println("The context " + ctxName + "is not present among the contexts defined");
		}
	}
	
	public void startDetectingContext(Context ctx){
		if(ctx == null) return;
			startDetectingContext(ctx.getName());
	}
	
	public void removeContext(String text) throws org.dei.perla.context.parser.ParseException  {
		String ctxName = ctxParser.removeContext(text);
		int index = getIndexContext(ctxName);
		if(index > -1){
			contexts.remove(index);
			refreshContext.remove(index);
		}
		else {
			System.out.println("There is not a context with the name " + ctxName);
		}
	}
	
	//quando il cd è pronto, bisogna prendere tutte le query delle clausole EVALUATED ON dei concetti e degli attributi	
	public void populateCDT(){
		for(Dimension d: cdt.getDimensions()){
			CreateAttr att = d.getAttribute();
			if(!CreateAttr.EMPTY.equals(att)){
				manageEvaluatedClause(att, d.getName());
			}
			else {
				for(Concept c: d.getConcepts()){
					c.getAttributes().forEach(a -> manageEvaluatedClause(a, d.getName()));
					Statement queryWhen = c.getWhen().getEvaluatedOn(); 
					String cdtHandlerName = concatenateWithDot(d.getName(), c.getName());
					//it retrieves the handler for the execution of the query 
					int index = this.cdtHandlerUtils.get(cdtHandlerName);
					try {
						queryExec.execute(queryWhen, cdtHandlers.get(index));
					} catch (QueryException e) {
						System.out.println("ERROR during the execution of the query for CONCEPT "
								+ c.getName() + " OF DIMENSION " + d.getName() + "\n");
						e.printStackTrace();
					} 
				}
			}
		}
	}
	
	private void manageEvaluatedClause(CreateAttr att, String dimension){
		if(att.getEvaluatedOn() instanceof QueryEvaluatedOn){
			QueryEvaluatedOn query = (QueryEvaluatedOn) att.getEvaluatedOn();
			Statement queryAtt = query.getQueryEvaluatedOn();
			String cdtHandlerName = concatenateWithDot(dimension, att.getName());
			int index = cdtHandlerUtils.get(cdtHandlerName);
			try {
				queryExec.execute(queryAtt, cdtHandlers.get(index));
			} catch (QueryException e) {
				System.out.println("ERROR during the execution of the query for ATTRIBUTE "
						+ att.getName() + " OF DIMENSION " + dimension + "\n");
				e.printStackTrace();
			} 
		}
		else if(att.getEvaluatedOn() instanceof FunctionEvaluatedOn) {
			FunctionEvaluatedOn function = (FunctionEvaluatedOn) att.getEvaluatedOn();
			String dimAttName = concatenateWithDot(dimension, att.getName());
			cache.put(dimAttName, function.computeValue());
		}
		else 
			throw new RuntimeException("Unexpected EVALUATION CLAUSE of ATTRIBUTE " + att.getName()
			+ " of DIMENSION " + dimension);
	}
	
	
	private class ContextElemAttHandler implements StatementHandler { 
		
		private final String dimension;
		private final String attribute;
		
		public ContextElemAttHandler(String dimension, String attribute) {
			this.dimension = dimension;
			this.attribute = attribute;
		}

		
	    public void complete(Statement s) {
			
		}

	    public void error(Statement s, Throwable cause){
			System.out.println("ERROR in retrieving ATTRIBUTE " + attribute + " of DIMENSION " + dimension 
			+ "\n" + cause.getMessage());
		}

		public void data(Statement stat, Record record) {
			Object value = record.getValues()[0];
			String dimensionAtt = "dimension" + "." + "attribute";
			cache.put(dimensionAtt, value);
		}
	}
	
	
	private class ContextElemSimpleHandler implements StatementHandler {
		
		private final String dimension;
		private final Concept concept;
		
		public ContextElemSimpleHandler(String dimension, Concept concept) {
			this.dimension = dimension;
			this.concept = concept;
		}

	    public void complete(Statement s) {
			
		}

	    public void error(Statement s, Throwable cause){
			System.out.println("ERROR during the execution of the query for CONCEPT " + concept.getName() 
					+ " of DIMENSION " + dimension + "\n" + cause.getMessage());
		}

	/*	
		*  l'utente DEVE elencare gli attributi della select nello stesso ordine della when condition
		*
		*/
		public void data(Statement stat, Record record) {
			Expression when = concept.getWhen().getWhen();
			LogicValue v = (LogicValue) when.run(record.getValues(), null);
			 if (v.toBoolean()) {
				cache.put(dimension, concept.getName()); 
	     	 }
		}
		
		
	}
	
	
}

