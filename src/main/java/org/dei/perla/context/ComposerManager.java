package org.dei.perla.context;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dei.perla.cdt.CDTUtils;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.PartialComponent;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.DataType;
import org.dei.perla.lang.parser.OnEmptySelection;
import org.dei.perla.lang.parser.ParserContext;
import org.dei.perla.lang.parser.ast.*;
import org.dei.perla.lang.query.expression.BoolOperation;
import org.dei.perla.lang.query.statement.RatePolicy;
import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.RefreshType;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.Statement;
import org.dei.perla.lang.query.statement.WindowSize;
import org.dei.perla.lang.query.statement.WindowSize.WindowType;

/**
 * <p> A helper class for the Context Manager responsible for keeping in memory contexts defined by the user. 
 * Contexts are divided in:
 * <ul>
 * 	<li> Undetected contexts, that are contexts that have been defined by the user, but the system has not started yet
 *  to detect them. In order to start the detection of a context, the user must invoke the {@code startDetectingContext}
 *  of the Context Manager class. </li>
 *  <li> Invalid contexts containing errors </li>
 * </ul> 
 * <p> The automatic composition of the contextual block is based on the partials associated, defined with the CDT. 
 *     It consists of different steps:
 * <ul> 
 * 	<li> As its first step, it retrieves all the relative context elements (the couples (Dimension = Value)
 * of the context and the relative partial components that are ENABLE, DISABLE and REFRESH.  </li>
 * <li> In the second step, the real composition of the different components is performed.
 * As far as the REFRESH component is concerned, the function computes the lowest refresh value among the ones 
 * contained in the Refresh list. 
 * The ENABLE and DISABLE components are formed by multiple clauses expressed using PerLa syntax. </li>
 * <li> In the last step, the contextual block is checked against some context-aware grammar rules that
 * ensure that the different Perla queries are valid. If there are not errors, the context is added to to the 
 * context lists that can be detected by the {@link ContextManager}, otherwise it is added to the invalid context list.
 * </li>
 * </ul>

 */

public class ComposerManager implements IComposerManager {
	
	private List<Context> possibleContexts;
	private List<Context> invalidContexts;
	
	//mappa che mostra gli errori associati ad un dato contesto
	Map<String, String> errors;
	
	public ComposerManager(){
		possibleContexts = new ArrayList<Context>();
		invalidContexts = new ArrayList<Context>();
		errors = new HashMap<String, String>();
	}
	
	public List<Context> getPossibleContexts(){
		return possibleContexts;
	}
	
	public List<Context> getInvalidContexts(){
		return invalidContexts;
	}
	
	public void addPossibleContext(Context ctx){
		composeBlock(ctx);
	}
	
	public void removePossibleContext(Context ctx){
		int index = getIndexContext(ctx.getName(), possibleContexts);
		if(index >= 0){
			possibleContexts.remove(index);
		}
		else {
			System.out.println("There is not a context with the name " + ctx.getName());
		}
	}
	
	public void addInvalidContext(Context ctx){
		invalidContexts.add(ctx);
	}
	
	public void removeInvalidContext(Context ctx){
		int index = getIndexContext(ctx.getName(), invalidContexts);
		if(index >= 0){
			invalidContexts.remove(index);
		}
		else {
			System.out.println("There is not a context with the name " + ctx.getName());
		}
	}
	
	private int getIndexContext(String ctxName, List<Context> contexts){
		for(int i=0; i < contexts.size(); i++){
			if(contexts.get(i).getName().equals(ctxName))
				return i;
		}
		return -1;
	}
	
	public Context getPossibleContext(String ctxName){
		for(Context c: possibleContexts){
			if(c.getName().equals(ctxName))
				return c;
		}
		return null;
	}
	
	public Context getInvalidContext(String ctxName){
		for(Context c: invalidContexts){
			if(c.getName().equals(ctxName))
				return c;
		}
		return null;
	}
	
	public String showErrorsInContext(Context ctxName){
		return errors.get(ctxName);
	}
	
	/*
	 * Before invoking this method, the user should first see the errors associated to the context by invoking
	 * the method @link showErrorsInContext
	 */
	public void correctContext(String ctxName, List<Statement> enable, List<Statement> disable, Refresh refresh){
		Context toCorrect = getInvalidContext(ctxName);
		if(toCorrect != null){
			toCorrect.setEnable(enable);
			toCorrect.setDisable(disable);
			toCorrect.setRefresh(refresh);
			possibleContexts.add(toCorrect);
			invalidContexts.remove(toCorrect);
		}
		else
			System.out.println("There is not a CONTEXT that has to be corrected with the name " + ctxName);
	}

	
	/*
	 * compone il blocco enable, disable e refresh
	 */
	public void composeBlock(Context context){
		List<Refresh> refreshes = new ArrayList<Refresh>();
		List<PartialComponent> enables = new ArrayList<PartialComponent>();
		List<PartialComponent> disables = new ArrayList<PartialComponent>();;
		Concept c = null;
		for(ContextElement ce: context.getContextElements()){
			if(ce instanceof ContextElemSimple){
				ContextElemSimple ces = (ContextElemSimple) ce;
				c = CDTUtils.getConcept(ces.getDimension(), ces.getValue());
				if(c != null){
					refreshes.add(c.getRefreshPeriod());
					enables.add(c.getEnableComponent());
					disables.add(c.getDisableComponent());
				}
			}
			else if(ce instanceof ContextElemAtt) {
				ContextElemAtt cea = (ContextElemAtt) ce;
				c = CDTUtils.getConceptByAttr(cea.getDimension(), cea.getAttribute());
				if(c != null){
					refreshes.add(c.getRefreshPeriod());
					enables.add(c.getEnableComponent());
					disables.add(c.getDisableComponent());
				}
			}
			else 
				throw new RuntimeException("The context elements has a run time Class unknown");
			}
		Refresh refresh = composeRefreshComponent(refreshes);
		ParserContext err = new ParserContext();
		List<Statement> enable = composeEnableComponent(context, enables, err);
		List<Statement> disable = composeDisableComponent(context, disables, err);
		context.setRefresh(refresh);
		context.setEnable(enable);
		context.setDisable(disable);
		if(!err.hasErrors())
			possibleContexts.add(context);
		else {
			invalidContexts.add(context);
			errors.put(context.getName(), err.getErrorDescription());
		}
	}
	
	public List<Statement> composeEnableComponent(Context context, 
			List<PartialComponent> enables, ParserContext err){
		List<Statement> stats = new ArrayList<Statement>();
		enables = enables.stream().filter(e-> e.getQuery() != "void").
				collect(Collectors.toList());
		List<SelectionStatementAST> sels = new ArrayList<SelectionStatementAST>();
	
		for(PartialComponent p: enables){
			if(p.getStatementAST() instanceof SelectionStatementAST){
				SelectionStatementAST sel = (SelectionStatementAST) p.getStatementAST();
				sels.add(sel);
			}
			else {
				Statement s = p.getStatementAST().compile(err);
				stats.add(s);
			}
		}
		if(!sels.isEmpty()){
			SelectionStatementAST selectionAST = (SelectionStatementAST) composeEnableComponent(sels, err);
			SelectionStatement sel = selectionAST.compile(err);
			stats.add(sel);
		}
		return stats;
	}
	
	public List<Statement> composeDisableComponent(Context context, 
			List<PartialComponent> disables, ParserContext err){
		List<Statement> stats = new ArrayList<Statement>();
		disables = disables.stream().filter(e->e.getQuery() != "void").
				collect(Collectors.toList());
	
		for(PartialComponent p: disables){
			if(p.getStatementAST() instanceof SetStatementAST){
				Statement s = p.getStatementAST().compile(err);
				stats.add(s);
			}
			else{
				err.addError("DISABLE block can only contain one shot queries of the type SET");
			}
		}
		return stats;
	}
	
	public Refresh composeRefreshComponent(List<Refresh> refreshes){
		refreshes = refreshes.stream().
				filter( r-> r.getType() != RefreshType.NEVER).collect(Collectors.toList());
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
	
	public StatementAST composeEnableComponent(List<SelectionStatementAST> sels, ParserContext ctx) {
		List<WindowSizeAST> everyList = new ArrayList<WindowSizeAST>();			
		List<FieldSelectionAST> fieldList = new ArrayList<FieldSelectionAST>();
		List<String> groupByFields = new ArrayList<String>();
		List<ExpressionAST> havingList = new ArrayList<ExpressionAST>();				
		List<WindowSizeAST> upToList = new ArrayList<WindowSizeAST>();
		List<SamplingAST> samplingList = new ArrayList<SamplingAST>();
		List<ExpressionAST> whereList = new ArrayList<ExpressionAST>();
		List<ExecutionConditionsAST> execCondList = new ArrayList<ExecutionConditionsAST>();
		List<WindowSizeAST> terminateList = new ArrayList<WindowSizeAST>();
		
		for(SelectionStatementAST sel: sels){
			everyList.add(sel.getEvery());
			fieldList.addAll(sel.getFields());
			if(sel.getGroupBy() != null){
				groupByFields.addAll(sel.getGroupBy().getFields());
			}
			havingList.add(sel.getHaving());
			upToList.add(sel.getUpto());
			samplingList.add(sel.getSamplingAST());
			whereList.add(sel.getWhere());
			execCondList.add(sel.getExecutionConditions());
			terminateList.add(sel.getTerminateAfter());
		}
		
		WindowSizeAST every = composeWindowSize(everyList, ctx);
		ExpressionAST having = composeHaving(havingList, ctx);				
		WindowSizeAST upto = composeWindowSize(upToList, ctx);
		OnEmptySelection oes = OnEmptySelection.NOTHING;
		SamplingAST sampling = composeSampling(samplingList, ctx);
		ExpressionAST where = composeWhere(whereList, ctx);
		ExecutionConditionsAST execCond = composeExecCond(execCondList, ctx);
		WindowSizeAST terminate = composeTerminate(terminateList, ctx);
		
		if(!groupByFields.isEmpty())
			return new SelectionStatementAST(every, fieldList, 
				new GroupByAST(groupByFields), having, upto, oes, sampling,
                where, execCond, terminate);
		else 
			return new SelectionStatementAST(every, fieldList, null, having, upto, oes, sampling,
	                where, execCond, terminate);
	}
	
	public WindowSizeAST composeWindowSize(List<WindowSizeAST> everyASTList, ParserContext ctx){
		if(everyASTList.isEmpty())
			return WindowSizeAST.ONE;
		ParserContext ctx2 = new ParserContext();
		WindowType type = everyASTList.get(0).getType();
		List<WindowSize> everyList = new ArrayList<WindowSize>();
		for(WindowSizeAST w: everyASTList){
			if(w.getType() != type){
				ctx2.addError("EVERY clauses must be of the same type");
				break;
			}
			everyList.add(w.compile(ctx2));
		}
		if(!ctx2.hasErrors()){
			WindowSize result = null;
			switch (type) {
	         case TIME: {
	             return findMinDuration(everyASTList, ctx);
	         }
	         case SAMPLE: {
	             result = findMinSamples(everyList, ctx);
	             WindowSizeAST aa = new WindowSizeAST(null, new ConstantAST(result.getSamples(), DataType.INTEGER));
	             System.out.println(result);
	             return aa;
	         }
	         default:
	             throw new RuntimeException("Unexpected WindowSize type " +
	                     type);
			}
		}
		else{
			ctx.addError(ctx2.getErrorDescription());
			return WindowSizeAST.ONE;
		}
	}
	
	private WindowSize findMinSamples(List<WindowSize> everyList, ParserContext ctx){
		Collections.sort(everyList, new Comparator<WindowSize>(){
		    public int compare(WindowSize wa, WindowSize wb){
		        if(wa.getSamples() > wb.getSamples())
		        	return 1;
		        if(wa.getSamples() < wb.getSamples())
		        	return -1;
		        else return 0;
		    }
		});
		return everyList.get(0);
	}
	
	private WindowSizeAST findMinDuration(List<WindowSizeAST> everyListAST, ParserContext ctx){
		Collections.sort(everyListAST, new Comparator<WindowSizeAST>(){
		    public int compare(WindowSizeAST wa, WindowSizeAST wb){
					Duration da = Duration.of(wa.getDurationValue().evalIntConstant(ctx), 
							wa.getDurationUnit());
					Duration db = Duration.of(wb.getDurationValue().evalIntConstant(ctx), 
							wb.getDurationUnit());
					return da.compareTo(db);
		    }
		});
		return everyListAST.get(0);
	}
	
	public ExpressionAST composeHaving(List<ExpressionAST> havingList, ParserContext ctx){
		if(havingList.isEmpty())
			return ConstantAST.TRUE;
		ExpressionAST having = havingList.get(0);
		int size = havingList.size();
		for(int i=1; i<size; i++){
			having = new BoolAST(BoolOperation.AND, having, havingList.get(i));
		}
		return having;
	}
	
	public SamplingAST composeSampling(List<SamplingAST> samplingList, ParserContext ctx){
		int event = 0;
		int ifEvery = 0;
		for(SamplingAST s: samplingList){
			if(s instanceof SamplingEventAST) event++;
			else ifEvery++;
		}
		if(event > 0 && ifEvery > 0)
			ctx.addError("SAMPLING clauses are incompatible");
		if(!ctx.hasErrors()){
			if(event > 0)
				return composeEventSampling(samplingList);
			else
				return composeIfEverySampling(samplingList, ctx);
		}
		return null;
	}
	
	private SamplingAST composeEventSampling(List<SamplingAST> samplingList){
		List<String> events = new ArrayList<String>();
		for(SamplingAST s: samplingList){
			SamplingEventAST e = (SamplingEventAST) s;
			events.addAll(e.getEvents());
		}
		return new SamplingEventAST(events);
	}
	
	private SamplingAST composeIfEverySampling(List<SamplingAST> samplingList, ParserContext ctx){
		List<IfEveryAST> ifevery = new ArrayList<IfEveryAST>();
		List<RefreshAST> refreshes = new ArrayList<RefreshAST>();
		SamplingIfEveryAST first = (SamplingIfEveryAST) samplingList.get(0);
		EveryAST min = first.getIfEvery().get(0).getEvery();
		Duration minDuration = Duration.of(min.getValue().evalIntConstant(new ParserContext()), 
				min.getUnit());
		for(SamplingAST s: samplingList){
			SamplingIfEveryAST e = (SamplingIfEveryAST) s;
			for(IfEveryAST ie : e.getIfEvery()){
				if(ie.getCondition() == ConstantAST.TRUE){
					EveryAST potentialMin = ie.getEvery();
					Duration d = Duration.of(potentialMin.getValue().evalIntConstant(new ParserContext()), 
							ie.getEvery().getUnit());
					if(d.compareTo(minDuration) < 0) {
						minDuration = d;
						min = potentialMin;
					}
				}
				else {
					ifevery.add(ie);
				}
			}
			refreshes.add(e.getRefresh());
		}
		ifevery.add(new IfEveryAST(ConstantAST.TRUE, min));
		RefreshAST refresh = this.findMinRefreshValue(refreshes, ctx);
		return new SamplingIfEveryAST(ifevery, RatePolicy.STRICT, refresh);
	}
	
	public ExpressionAST composeWhere(List<ExpressionAST> whereList, ParserContext ctx){
		if(whereList.isEmpty())
			return ConstantAST.TRUE;
		ExpressionAST where = whereList.get(0);
		int size = whereList.size();
		for(int i=1; i<size; i++){
			where = new BoolAST(BoolOperation.AND, where, whereList.get(i));
		}
		return where;
	}
	
	public ExecutionConditionsAST composeExecCond(
			List<ExecutionConditionsAST> execCondList, ParserContext ctx){
		if(execCondList.isEmpty())
			return ExecutionConditionsAST.ALWAYS;
		List<ExpressionAST> conds = new ArrayList<ExpressionAST>();
		List<Attribute> atts = new ArrayList<Attribute>();
		List<RefreshAST> refreshes = new ArrayList<RefreshAST>();
		for(ExecutionConditionsAST e: execCondList){
			conds.add(e.getCondition());
			atts.addAll(e.getSpecifications().getSpecifications());
			refreshes.add(e.getRefresh());
		}
		ExpressionAST cond = conds.get(0);
		int size = conds.size();
		for(int i = 1; i<size; i++){
			cond = new BoolAST(BoolOperation.AND, cond, conds.get(i));
		}
		NodeSpecificationsAST specs = new NodeSpecificationsAST(atts);
		RefreshAST refresh = findMinRefreshValue(refreshes, ctx);
		return new ExecutionConditionsAST(cond, specs, refresh);
	}
	
	private RefreshAST findMinRefreshValue(List<RefreshAST> refreshes, ParserContext ctx){
		RefreshType type = RefreshType.NEVER;
		int event = 0;
		int time = 0;
		for(RefreshAST r: refreshes){
			if(r.getType() == RefreshType.TIME){
				type = RefreshType.TIME;
				time++;
			}
			if(r.getType() == RefreshType.EVENT){
				event++;
				type = RefreshType.EVENT;
			}
		}
		if(time>0 && event>0){
			ctx.addError("REFRESH clauses in EXECUTION CONDITION clauses are incompatible");
		}
		else{
			switch (type) {
	        case NEVER:
	            return RefreshAST.NEVER;
	        case EVENT: {
	        	List<String> events = new ArrayList<String>();
	            for(RefreshAST r: refreshes){
	            	events.addAll(r.getEvents());
	            }
	            return new RefreshAST(null, events);
	            }
	        case TIME: 
	        	return find(refreshes, ctx);
	        default:
	            throw new RuntimeException("Unknown refresh type " + type);
			}
		}
		return null;
	}
	
	private RefreshAST find(List<RefreshAST> refreshes, ParserContext ctx){
		refreshes = refreshes.stream().filter(e -> e.getType()!=RefreshType.NEVER).collect(Collectors.toList());
		Collections.sort(refreshes, new Comparator<RefreshAST>(){
		    public int compare(RefreshAST ra, RefreshAST rb){
		        Duration da = Duration.of(ra.getDurationValue().evalIntConstant(ctx),
		        		ra.getDurationUnit());
		        Duration db = Duration.of(rb.getDurationValue().evalIntConstant(ctx),
		        		rb.getDurationUnit());
		        return da.compareTo(db);
		    }
			});
		return refreshes.get(0);
	}
	
	public WindowSizeAST composeTerminate(List<WindowSizeAST> terminateList,
			ParserContext ctx){
		terminateList = terminateList.stream().filter(t -> t!=null).collect(Collectors.toList());
		if(terminateList.isEmpty())
			return null;
		return this.composeWindowSize(terminateList, ctx);
	}

}
