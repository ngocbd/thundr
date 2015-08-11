package com.threewks.thundr.aop;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;

public class AdviceRegistryImpl implements AdviceRegistry {
	private Map<Class<? extends Annotation>, Advice<?, ?>> advice = new HashMap<>();

	@Override
	public <A extends Annotation> void add(Class<A> annotation, Advice<A, ?> interceptor) {
		advice.put(annotation, interceptor);
	}

	@Override
	public <A extends Annotation> void remove(Class<A> annotation) {
		advice.remove(annotation);
	}

	@Override
	public boolean contains(Class<? extends Annotation> annotation) {
		return advice.containsKey(annotation);
	}

	@Override
	public <A extends Annotation> boolean contains(Class<A> annotation, Class<? extends Advice<? extends A, ?>> interceptor) {
		Advice<?, ?> ad = advice.get(annotation);
		return ad != null && ad.getClass() == interceptor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T proxyIfNeeded(T instance) {
		AdviceMethodInterceptor interceptor = new AdviceMethodInterceptor(instance, advice);
		return interceptor.hasAdvice() ? (T) Enhancer.create(instance.getClass(), interceptor) : instance;
	}
}
