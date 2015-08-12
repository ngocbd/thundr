/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import com.threewks.thundr.introspection.ClassIntrospector;
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
			ClassIntrospector classIntrospector = new ClassIntrospector();
			List<Method> methods = classIntrospector.listMethods(delegate.getClass());
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
