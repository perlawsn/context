package org.dei.perla.cdt;
/**
 * <p>
 * A generic interface for specifying how an <code>Attribute</code> is retrieved.
 * A default <code>EvaluatedOn</code> implementation is not supplied
 * since an <code>Attribute</code> might be retrived at run time, for example by 
 * executing a suitable Query Perla, or at design time, for example by extracting its 
 * value from xml file.
 * </p>
 *
 * <p>
 * A single <code>EvaluatedOn</code> contains the String inserted by the user when he
 * creates an attribute and specifies with the "EVALUATED ON" clause how the attribute
 * must be evaluated.
 * </p>
 *
 * @author Guido Rota (2014)
 *
 */
public interface EvaluatedOn {
	
	public String getEvaluatedOn();

}
