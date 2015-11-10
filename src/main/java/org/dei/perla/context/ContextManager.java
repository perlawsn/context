package org.dei.perla.context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.cdt.parser.CDTreeParser;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.parser.ContextParser;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.SetStatementAST;
import org.dei.perla.lang.parser.ast.StatementAST;
import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.Statement;

public class ContextManager {
	
	private final CDTreeParser cdtParser;
	private CDT cdt;
	private final ContextParser ctxParser;
	private List<Context> contexts;
	private List<Context> invalidContexts;
	private ComposeBlock composeBlock;
	private List<RefreshContext> refreshContext;
	private final ContextExecutor ctxExecutor;
	
	
	public ContextManager() {
		cdt = null;
		cdtParser = new CDTreeParser();
		ctxParser = new ContextParser();
		contexts = new ArrayList<Context>();
		invalidContexts = new ArrayList<Context>();
		composeBlock = new ComposeBlock();
		refreshContext = new ArrayList<RefreshContext>();
		ctxExecutor = new ContextExecutor();
	}
	
	public CDT getCDT(){
		return cdt;
	}
	
	public List<Context> getContexts() {
		return contexts;
	}
	
	public List<Context> getInvalidContexts() {
		return invalidContexts;
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
	
	public void createCDT(String text) throws ParseException{
		cdt = cdtParser.parse(text);
	}

	//aggiungere controllo sul nome
	public void createContext(String text) throws org.dei.perla.context.parser.ParseException {
		if(cdt==null)
			throw new RuntimeException("Before creating a context it is necessary to create the CDT");
		Context ctx = ctxParser.parse(text);
		composeBlock(ctx);
		if(ctx.getValidDisable() && ctx.getValidEnable()) {
			ctx.addObserver(ctxExecutor);
			contexts.add(ctx);
			RefreshContext r = new RefreshContext(ctx);
			refreshContext.add(r);
			r.start();
		}
		else
			invalidContexts.add(ctx);
	}
	
	public void removeContext(String ctxName) {
		int index = getIndexContext(ctxName);
		if(index > 0){
			contexts.remove(index);
			refreshContext.remove(index);
		}
	}

	public void changeContext(String ctxName){
		removeContext(ctxName);
	}
	
	public void correctContext(String ctxName, List<StatementAST> enableAST, List<StatementAST> disableAST){
		List<Statement> enable = new ArrayList<Statement>();
		List<Statement> disable = new ArrayList<Statement>();
		ParserContext ctx = new ParserContext();
		Statement s = null;
		for(StatementAST ast: enableAST){
			s = ast.compile(ctx);
			enable.add(s);
		}
		for(StatementAST ast: disableAST){
			s = ast.compile(ctx);
			disable.add(s);
		}
		if(ctx.hasErrors())
			System.out.println("Query Perla contains the folliwing errors: " + ctx.getErrorDescription());
		else {
			Context toCorrect = null;
			for(Context c: invalidContexts){
				if(c.getName().equals(ctxName)){
					toCorrect = c;
					break;
				}
			}
			toCorrect.setEnable(enable);
			toCorrect.setDisable(disable);
			toCorrect.setValidDisable(true);
			toCorrect.setValidEnable(true);
			toCorrect.addObserver(ctxExecutor);
			contexts.add(toCorrect);
			RefreshContext r = new RefreshContext(toCorrect);
			refreshContext.add(r);
			r.start();
		}
	}

	private List<Statement> composeEnableComponent(Context context, List<PartialComponent> enables){
		ParserContext ctxSel = new ParserContext();
		ParserContext ctx = new ParserContext();
		List<Statement> stats = new ArrayList<Statement>();
		enables = enables.stream().filter(e->e.equals(PartialComponent.EMPTY)).
				collect(Collectors.toList());
		List<SelectionStatementAST> sels = new ArrayList<SelectionStatementAST>();
	
		for(PartialComponent p: enables){
			if(p.getStatement() instanceof SelectionStatementAST){
				SelectionStatementAST sel = (SelectionStatementAST) p.getStatement();
				sels.add(sel);
			}
			else {
				Statement s = p.getStatement().compile(ctx);
				stats.add(s);
			}
		}
		SelectionStatementAST selectionAST = (SelectionStatementAST) composeBlock.composeEnableComponent(sels, ctxSel);
		SelectionStatement sel = selectionAST.compile(ctxSel);
		if(ctx.hasErrors() || ctxSel.hasErrors()){
			context.setValidEnable(false);
			System.out.println("ENABLE BLOCK is not semantically and/or syntactically correct, "
					+ "the following errors have been signaled: \n" + ctx.getErrorDescription() 
					+ "\n" + ctxSel.getErrorDescription());
			System.out.println("\nThe user must correct these errors manually otherwise the context will not "
					+ "be accepted ");
		} else
			context.setValidEnable(true);
		stats.add(sel);
		return stats;
	}
	
	private List<Statement> composeDisableComponent(Context context, List<PartialComponent> disables){
		ParserContext ctx = new ParserContext();
		List<Statement> stats = new ArrayList<Statement>();
		disables = disables.stream().filter(e->e.equals(PartialComponent.EMPTY)).
				collect(Collectors.toList());
	
		for(PartialComponent p: disables){
			if(p.getStatement() instanceof SetStatementAST){
				Statement s = p.getStatement().compile(ctx);
				stats.add(s);
			}
			else{
				ctx.addError("DISABLE block can only contain one shot queries of the type SET");
			}
		}
		if(ctx.hasErrors()){
			context.setValidDisable(false);
			System.out.println(ctx.getErrorDescription());
			System.out.println("\nThe user must correct these errors manually ");
		} else {
			context.setValidDisable(true);
		}
		return stats;
	}
	
	private Refresh composeRefreshComponent(List<Refresh> refreshes){
		if(refreshes.isEmpty())
			return Refresh.NEVER;
		Collections.sort(refreshes, new Comparator<Refresh>(){
		    public int compare(Refresh ra, Refresh rb){
		        Duration da = ra.getDuration();
		        Duration db = rb.getDuration(); 
		        return da.compareTo(db);
		    }
			});
		return refreshes.get(0);
	}

	
	private void composeBlock(Context context){
		List<ContextElemSimple> simpleElements = new ArrayList<ContextElemSimple>();
		for(ContextElement c: context.getContextElements()){
			if(c instanceof ContextElemSimple)
				simpleElements.add((ContextElemSimple) c);
		}
		List<Refresh> refreshes = new ArrayList<Refresh>();
		List<PartialComponent> enables = new ArrayList<PartialComponent>();
		List<PartialComponent> disables = new ArrayList<PartialComponent>();;
		for(ContextElemSimple ce: simpleElements){
			Concept c = cdt.getConceptOfDim(ce.getDimension(), ce.getValue());
			if(c != null){
				refreshes.add(c.getRefreshPeriod());
				enables.add(c.getEnableComponent());
				disables.add(c.getDisableComponent());
			}
		}
		Refresh refresh = composeRefreshComponent(refreshes);
		List<Statement> enable = composeEnableComponent(context, enables);
		List<Statement> disable = composeDisableComponent(context, disables);
		context.setRefresh(refresh);
		context.setEnable(enable);
		context.setDisable(disable);
	}
	
	//quando il cd è pronto, bisogna prendere tutte le query delle condizioni when dei concetti e degli attributi
	public void populateCDT(){
		for(Dimension d: cdt.getDimensions()){
			CreateAttr att = d.getAttribute();
			if(att.equals(CreateAttr.EMPTY)){
				if(att.getEvaluatedOn() instanceof QueryEvaluatedOn){
					QueryEvaluatedOn query = (QueryEvaluatedOn) att.getEvaluatedOn();
					StatementAST queryAtt = query.getQueryEvaluatedOn();
					//da inviare all'esecutore
					//una volta ricevuto i risultati, assegnare il valore all'attributo
				}
				else 
					throw new RuntimeException("it's impossible to retrieve the attribute");
			}
			else {
				for(Concept c: d.getConcepts()){
					StatementAST when = c.getWhen().getEvaluatedOn();
					//da inviare all'esecutore
					//una volta ricevuti i risultati, valutare quale Concetto assegnare alla dimensione
				}
			}
		}
		
	}
	


}
