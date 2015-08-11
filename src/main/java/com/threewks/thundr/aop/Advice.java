package com.threewks.thundr.aop;

import java.lang.annotation.Annotation;

public interface Advice<A extends Annotation, T> {

	/**
	 * Called before the method with a pointcut on it is run. The given {@link Arguments} can be read and mutated before the method is called.
	 * Advice is run in order of the annotations on the method. If any advice returns a non-null value, no further before advice will be invoked, nor will
	 * the actual method.
	 * 
	 * @param annotation
	 * @param arguments
	 * @return null to continue processing the pointcut chain and invoke the real method, any other value will behave as if returned from the real methodf
	 */
	public T before(A annotation, Arguments arguments);

	/**
	 * Called after the real method has been invoked, in reverse order of the annotations on the method.
	 * The value returned by this method is passed to the next after advice, the final one being returned as though
	 * returned by the real method.
	 * 
	 * @param result
	 * @param annotation
	 * @param arguments
	 * @return the given result parameter, or another value to change the method invocation result.
	 */
	public T after(T result, A annotation, Arguments arguments);

	/**
	 * Called if a before, after pointcut or the real method throws an exception. They are invoked in reverse order of the annotation on the real
	 * method. The first advice to return a non-null value will be the last to process the exception, and the value will be returned as though
	 * return from the real method
	 * 
	 * @param e
	 * @param annotation
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	public T exception(Exception e, A annotation, Arguments arguments);
}
