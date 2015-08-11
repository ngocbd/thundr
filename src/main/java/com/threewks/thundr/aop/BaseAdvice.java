package com.threewks.thundr.aop;

import java.lang.annotation.Annotation;

public class BaseAdvice<A extends Annotation, T> implements Advice<A, T> {

	@Override
	public T before(A annotation, Arguments arguments) {
		return null;
	}

	@Override
	public T after(T result, A annotation, Arguments arguments) {
		return result;
	}

	@Override
	public T exception(Exception e, A annotation, Arguments arguments) {
		return null;
	}

}
