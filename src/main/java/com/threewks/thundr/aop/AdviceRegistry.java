package com.threewks.thundr.aop;

import java.lang.annotation.Annotation;

public interface AdviceRegistry {
	public <A extends Annotation> void add(Class<A> annotation, Advice<A, ?> interceptor);

	public <A extends Annotation> void remove(Class<A> annotation);

	public boolean contains(Class<? extends Annotation> annotation);

	public <A extends Annotation> boolean contains(Class<A> annotation, Class<? extends Advice<? extends A, ?>> interceptor);

	/**
	 * Given an instance, will wrap it in a proxy which will execute pointcuts when annotated methods are called, if any pointcuts exist.
	 * If not, the original instance is returned
	 * 
	 * @param instance
	 * @return
	 */
	public <T> T proxyIfNeeded(T instance);

}