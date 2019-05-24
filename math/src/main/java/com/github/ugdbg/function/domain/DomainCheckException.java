package com.github.ugdbg.function.domain;

import com.github.ugdbg.function.scalar.Function;
import com.github.ugdbg.function.vector.VFunction;

/**
 * Exception that can be thrown when a input value is outside the domain of a function.
 */
public class DomainCheckException extends RuntimeException {
	/** the domain */
	private Domain<?> domain;
	
	/** the input value */
	private Object input;

	/**
	 * Complete constructor.
	 * @param domain the domain
	 * @param input  the input value
	 */
	public DomainCheckException(Domain<?> domain, Object input) {
		super("The value [" + input + "] is outside the domain " + domain + "");
	}

	/**
	 * Convenience constructor for a function. 
	 * The message includes the domain, the input value and the function {@link Function#label()}.
	 * @param function the function 
	 * @param input    the input value
	 */
	public DomainCheckException(Function function, Object input) {
		super("The value [" + input + "]" 
			+ " is outside the domain " + function.domain() + "" 
			+ " of function [" + function.label() + "]");
	}
	
	/**
	 * Convenience constructor for a function. 
	 * The message includes the domain, the input value and the function {@link Function#label()}.
	 * @param function the function 
	 * @param input    the input value
	 */
	public DomainCheckException(VFunction function, Object input) {
		super("The value [" + input + "]" 
			+ " is outside the domain " + function.domain() + "" 
			+ " of function [" + function.label() + "]");
	}

	/**
	 * Get the domain check exception domain.
	 * @return {@link #domain}
	 */
	public Domain<?> getDomain() {
		return this.domain;
	}

	/**
	 * Get the domain check exception input value
	 * @return {@link #input}
	 */
	public Object getInput() {
		return this.input;
	}
}
