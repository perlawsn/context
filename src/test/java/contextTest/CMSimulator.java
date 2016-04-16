package contextTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.FunctionEvaluatedOn;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.cdt.parser.CDTreeParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextDetector;
import org.dei.perla.context.ContextExecutor;
import org.dei.perla.context.IComposerManager;
import org.dei.perla.context.IConflictDetector;
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


public class CMSimulator {
	
	private final CDTreeParser cdtParser;
	private CDT cdt;
	private final ContextParser ctxParser;
	
	private List<Context> contexts;
	
	private final IComposerManager composerMgr;
	private List<ContextDetector> refreshContext;
	private final ContextExecutor ctxExecutor;
	
    private final Executor queryExec;

	//cache for quickly retrieving the current value of a dimension
	private static Map<String, Set<Object>> cache;
	
	private Map<String, Integer> cdtHandlerUtils;
	private List<StatementHandler> cdtHandlers;
	
	public CMSimulator(PerLaSystem system, IComposerManager composerMgr, IConflictDetector conflictDetector) {
		cdt = null;
		cdtParser = new CDTreeParser();
		ctxParser = new ContextParser();
		contexts = new ArrayList<Context>();
		refreshContext = new ArrayList<ContextDetector>();
		ctxExecutor = new ContextExecutor(conflictDetector);
		this.composerMgr = composerMgr;
		cache = new ConcurrentHashMap<String, Set<Object>>();
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
	
	//serve per i test
	public void addConceptToCache(String dimension, String concept){
		Set concepts = (Set) cache.get(dimension);
		concepts.add(concept);
	}
	
	//serve per i test
	public void removeConceptToCache(String dimension, String concept){
		Set concepts = (Set) cache.get(dimension);
		concepts.remove(concept);
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
	
	/*
	 * returns the shallow copy of the cache containing the value of the Dimension and attribute
	 * at the moment of the method invocation
	 */
	public Map<String, Object> getCache(){
		Map<String, Object> shallowCopy = new HashMap<String, Object>();
		shallowCopy.putAll(cache);
		return shallowCopy;
	}
	
	public Map<String, Integer> getHandlers(){
		Map<String, Integer> shallowCopy = new HashMap<String, Integer>();
		shallowCopy.putAll(this.cdtHandlerUtils);
		return shallowCopy;
	}
	
	public void createCDT(String text) throws ParseException{
		cdt = cdtParser.parseCDT(text);
		initCache(cdt);
	}
	
	public void createCDTFromFile(String path) throws ParseException{
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			String cdtText = IOUtils.toString(br);
			cdt = cdtParser.parseCDT(cdtText);
			initCache(cdt);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Impossible to read the file " + path);
		} 
	}
	
	public void addDimension(String text){
		if(cdt==null)
			throw new RuntimeException("Before adding a DIMENSION, it is necessary to create the CDT");
		Dimension dim;
		try {
			dim = cdtParser.parseAddDimension(text);
			cdt.addDimension(dim);
			boolean hasConcept = false;
			int index = cdtHandlerUtils.size();
			if(dim.getAttribute() == (CreateAttr.EMPTY)){
				for(Concept c: dim.getConcepts()){
					if(c.getWhen().getWhen() != null) hasConcept = true;
					String dimConceptName = concatenateWithDot(dim.getName(), c.getName());
					cdtHandlerUtils.put(dimConceptName, index);
					cdtHandlers.add(new ContextElemSimpleHandler(dim.getName(), c));
					index++;
					for(CreateAttr attr: c.getAttributes()){
						String dimAttName = concatenateWithDot(dim.getName(), attr.getName());
						cache.put(dimAttName, new HashSet<>());
						cdtHandlerUtils.put(dimAttName, index);
						cdtHandlers.add(new ContextElemAttHandler(dim.getName(), attr.getName()));
						index++;
					}
				}
			} else {
				String dimAttName = concatenateWithDot(dim.getName(), dim.getAttribute().getName());
				cache.put(dimAttName, new HashSet<>());
				cdtHandlerUtils.put(dimAttName, index);
				cdtHandlers.add(new ContextElemAttHandler(dim.getName(),  dim.getAttribute().getName()));
			}
			if(hasConcept) 
				cache.put(dim.getName(), new HashSet<>());
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
			Dimension d = cdt.getDimByName(dim);
			if(d != null){
				cache.remove("dim");
				if(d.getAttribute() != CreateAttr.EMPTY){
					String dimAttName = concatenateWithDot(dim, d.getAttribute().getName());
					cache.remove(dimAttName);
				}
				cdt.removeDimension(d);
			}
			//non rimuovo gli handlers perche' altrimenti la lista shifta e perderei l'ordinamento
		} catch (ParseException e) {
			System.out.println("ERROR during the parsing of the request");
			e.printStackTrace();
		}
	}
	
	public void createContexts(String text) throws org.dei.perla.context.parser.ParseException {
		if(cdt==null)
			throw new RuntimeException("Before creating a context it is necessary to create the CDT");
		List<Context> ctxs = ctxParser.create(text);
		for(Context c: ctxs){
			composerMgr.addPossibleContext(c);
		}
	}

	public void createContextsFromFile(String path) throws org.dei.perla.context.parser.ParseException {
		if(cdt==null)
			throw new RuntimeException("Before creating a context it is necessary to create the CDT");
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			String contexts = IOUtils.toString(br);
			List<Context> ctxs = ctxParser.create(contexts);
			for(Context c: ctxs){
				composerMgr.addPossibleContext(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Impossible to read the file " + path);
		} 
	}
	
	private void initCache(CDT cdt){
		int i = 0;
		String dimAttName;
		String dimConceptName;
		boolean hasConcept;
		for(Dimension d: cdt.getDimensions()){
			hasConcept = false;
			if(d.getAttribute() != CreateAttr.EMPTY){
				CreateAttr att = d.getAttribute();
				dimAttName = concatenateWithDot(d.getName(), att.getName());
				cache.put(dimAttName, new HashSet<>());
				cdtHandlerUtils.put(dimAttName, i);
				cdtHandlers.add(new ContextElemAttHandler(d.getName(), att.getName()));
				i++;
			}
			else {
				for(Concept c: d.getConcepts() ){ 
					if(c.getWhen().getWhen() != null) hasConcept = true;
					dimConceptName = concatenateWithDot(d.getName(), c.getName());
					cdtHandlerUtils.put(dimConceptName, i);
					cdtHandlers.add(new ContextElemSimpleHandler(d.getName(), c));
					i++;
					for(CreateAttr attr: c.getAttributes()){
						dimAttName = concatenateWithDot(d.getName(), attr.getName());
						cache.put(dimAttName, new HashSet<>());
						cdtHandlerUtils.put(dimAttName, i);
						cdtHandlers.add(new ContextElemAttHandler(d.getName(), attr.getName()));
						i++;
					}
				}
			}
			if(hasConcept)
				cache.put(d.getName(), new HashSet<>());
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
			ctxExecutor.addContextToExecute(toDetect);
			composerMgr.removePossibleContext(toDetect);
			ContextDetector r = new ContextDetector(toDetect, cache);
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
					if(queryWhen != null) {
						String cdtHandlerName = concatenateWithDot(d.getName(), c.getName());
						//it retrieves the handler for the execution of the query 
						int index = this.cdtHandlerUtils.get(cdtHandlerName);
						try {
							System.out.println(c + " " + queryWhen);
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
			Object value = function.computeValue();
			synchronized(cache.get(dimAttName)){
				Set values = cache.get(dimAttName);
				values.add(value);
			}
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

	    public void error(Statement s, Throwable cause){
			System.out.println("ERROR in retrieving ATTRIBUTE " + attribute + " of DIMENSION " + dimension 
			+ "\n" + cause.getMessage());
		}

		public void data(Statement stat, Record record) {
			Object value = record.getValues()[0];
			String dimensionAtt = dimension + "." + attribute;
			synchronized(cache.get(dimensionAtt)){
				Set values = cache.get(dimensionAtt);
				values.add(value);
			}
		}

		@Override
		public void complete() {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	private class ContextElemSimpleHandler implements StatementHandler {
		
		private final String dimension;
		private final Concept concept;
		
		public ContextElemSimpleHandler(String dimension, Concept concept) {
			this.dimension = dimension;
			this.concept = concept;
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
			System.out.println("sono dentro data di " + dimension + "." + concept + " logicValue " + v);
				synchronized(cache.get(dimension)){
				Set concepts = cache.get(dimension);
				 if (v.toBoolean()) 
						concepts.add(concept.getName());
				 else 
					 concepts.remove(concept.getName());
			}
		}

		@Override
		public void complete() {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
	
}


