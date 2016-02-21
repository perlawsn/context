package org.dei.perla.context;

public class ContextParseException extends Exception {

	private static final long serialVersionUID = -5370811054706993820L;
	
	  public ContextParseException() {
	        super();
	    }
	
	    public ContextParseException(String msg) {
	        super(msg);
	    }
	
	    public ContextParseException(Throwable cause) {
	        super(cause);
	    }
	
	    public ContextParseException(String msg, Throwable cause) {
	        super(msg, cause);
	    }

}
