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
import org.dei.perla.lang.parser.ast.ExecutionConditionsAST;
import org.dei.perla.lang.parser.ast.ExpressionAST;
import org.dei.perla.lang.parser.ast.FieldSelectionAST;
import org.dei.perla.lang.parser.ast.GroupByAST;
import org.dei.perla.lang.parser.ast.SamplingAST;
import org.dei.perla.lang.parser.ast.SelectionStatementAST;
import org.dei.perla.lang.parser.ast.WindowSizeAST;
import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.Statement;

public class ContextManager {
	
	private CDT cdt;
	private List<Context> contexts;
	private Context activeContext;
	
	public ContextManager(CDT cdt) {
		this.cdt = cdt;
		contexts = new ArrayList<Context>();
		activeContext = null;
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
	
	/*
	 * SELECTION								INSERTION
	 * WindowSizeAST every;					String stream, List<String> field, SelectionStatement
     List<FieldSelectionAST> fields;
     GroupByAST groupBy;
     ExpressionAST having;				CREATION: UGUALE A insertion
     WindowSizeAST upto;
     OnEmptySelection oes;
     SamplingAST sampling;
     ExpressionAST where;
     ExecutionConditionsAST execCond;
     WindowSizeAST terminate;
	 */
	private List<Statement> composeEnableComponent(List<PartialComponent> enables){
		enables = enables.stream().filter(e -> e.getStatement()!=null).collect(Collectors.toList());
		List<SelectionStatementAST> sels = new ArrayList<SelectionStatementAST>();
		for(PartialComponent p: enables){
			if(p.getStatement() instanceof SelectionStatementAST){
				SelectionStatementAST sel = (SelectionStatementAST) p.getStatement();
				sels.add(sel);
			}
		}
		
		return null;
	}
	
	private List<Statement> composeDisableComponent(List<PartialComponent> disables){
		return null;
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
	/*
	 * quando il contextparser dà esito positivo, la funzione compone il blocco enable, disable e refresh
	 * dato in input un contesto, per ogni context element del tipo Dimension = concept 
	 * si prendono i partial component e refresh
	 */
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
		List<Statement> enable = composeEnableComponent(enables);
		List<Statement> disable = composeDisableComponent(disables);
		context.setRefresh(refresh);
		context.setEnable(enable);
		context.setDisable(disable);
	}

}
