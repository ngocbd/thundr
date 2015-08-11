package com.threewks.thundr.aop;

import static com.atomicleopard.expressive.Expressive.isNotEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.introspection.MethodIntrospector;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class AdviceMethodInterceptor implements MethodInterceptor {
	protected Object delegate;
	protected Map<Method, Map<Annotation, Advice<Annotation, Object>>> proxyMethods = new HashMap<>();
	protected Map<Method, MethodIntrospector> introspectors = new HashMap<>();

	public AdviceMethodInterceptor(Object delegate, Map<Class<? extends Annotation>, Advice<?, ?>> advice) {
		this.delegate = delegate;
		intialisePointcuts(delegate, advice);
	}

	public boolean hasAdvice() {
		return !this.proxyMethods.isEmpty();
	}

	@SuppressWarnings("unchecked")
	protected void intialisePointcuts(Object delegate, Map<Class<? extends Annotation>, Advice<?, ?>> advice) {
		if (delegate != null) {
			Method[] methods = delegate.getClass().getMethods();
			for (Method method : methods) {
				Map<Annotation, Advice<Annotation, Object>> pointcuts = new LinkedHashMap<>();
				for (Annotation annotation : method.getAnnotations()) {
					Class<? extends Annotation> annotationClass = annotation.annotationType();
					Advice<Annotation, Object> adv = (Advice<Annotation, Object>) advice.get(annotationClass);
					if (adv != null) {
						pointcuts.put(annotation, adv);
					}
				}
				if (isNotEmpty(pointcuts)) {
					proxyMethods.put(method, pointcuts);
					introspectors.put(method, new MethodIntrospector(method));
				}
			}
		}
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Map<Annotation, Advice<Annotation, Object>> pointcuts = proxyMethods.get(method);
		MethodIntrospector introspector = introspectors.get(method);
		Arguments arguments = new ArgumentsImpl(introspector, args);
		try {
			Object result = before(pointcuts, arguments);
			if (result == null) {
				result = proxy.invoke(delegate, arguments.toArgs());
			}
			result = after(pointcuts, arguments, result);
			return result;
		} catch (Exception e) {
			Object result = exception(pointcuts, arguments, e);
			if (result != null) {
				return result;
			}
			throw e;
		}
	}

	protected Object exception(Map<Annotation, Advice<Annotation, Object>> pointcuts, Arguments arguments, Exception e) throws Exception {
		if (pointcuts != null) {
			List<Annotation> annotations = new ArrayList<>(pointcuts.keySet());
			Collections.reverse(annotations);
			for (Annotation annotation : annotations) {
				Advice<Annotation, Object> advice = pointcuts.get(annotation);
				Object result = advice.exception(e, annotation, arguments);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	protected Object after(Map<Annotation, Advice<Annotation, Object>> pointcuts, Arguments arguments, Object result) {
		if (pointcuts != null) {
			List<Annotation> annotations = new ArrayList<>(pointcuts.keySet());
			Collections.reverse(annotations);
			for (Annotation annotation : annotations) {
				Advice<Annotation, Object> advice = pointcuts.get(annotation);
				result = advice.after(result, annotation, arguments);
			}
		}
		return result;
	}

	protected Object before(Map<Annotation, Advice<Annotation, Object>> pointcuts, Arguments arguments) {
		Object result = null;
		if (pointcuts != null) {
			for (Annotation annotation : pointcuts.keySet()) {
				Advice<Annotation, ?> advice = pointcuts.get(annotation);
				result = advice.before(annotation, arguments);
				if (result != null) {
					return result;
				}
			}
		}
		return result;
	}
}
