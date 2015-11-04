package org.dei.perla.context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.lang.parser.OnEmptySelection;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.CreationStatementAST;
import org.dei.perla.lang.parser.ast.ExecutionConditionsAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.FieldSelectionAST;
import org.dei.perla.lang.parser.ast.GroupByAST;
import org.dei.perla.lang.parser.ast.InsertionStatementAST;
import org.dei.perla.lang.parser.ast.SamplingAST;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.SetStatementAST;
import org.dei.perla.lang.parser.ast.WindowSizeAST;
import org.dei.perla.lang.query.statement.CreationStatement;
import org.dei.perla.lang.query.statement.InsertionStatement;
import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.SetStatement;
import org.dei.perla.lang.query.statement.Statement;

public class ContextManager {
	
	private CDT cdt;
	private List<Context> contexts;
	private Context activeContext;
	private ComposeBlock composeBlock;
	
	public ContextManager(CDT cdt) {
		this.cdt = cdt;
		contexts = new ArrayList<Context>();
		activeContext = null;
		composeBlock = new ComposeBlock();
	}
	
	public CDT getCDT(){
		return cdt;
	}
	
	public List<Context> getContexts() {
		return contexts;
	}
	
	public Context getActiveContext(){
		return activeContext;
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
			System.out.println("\nThe user must correct these errors manually ");
		}
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

	
	public void composeBlock(Context context){
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
	
	//to do
	public void populateCDT(){
		
	}
	
	//to do
	public void executeActiveContextAction(){
		
	}

}
