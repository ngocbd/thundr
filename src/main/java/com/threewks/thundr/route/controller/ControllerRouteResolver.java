/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.route.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.BinderRegistry;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.route.RouteResolver;
import com.threewks.thundr.route.RouteResolverException;

public class ControllerRouteResolver implements RouteResolver<Controller>, InterceptorRegistry {

	private Map<Class<?>, Object> controllerInstances = new HashMap<Class<?>, Object>();
	private Map<Class<? extends Annotation>, Interceptor<? extends Annotation>> actionInterceptors = new HashMap<Class<? extends Annotation>, Interceptor<? extends Annotation>>();
	private Map<Method, Map<Annotation, Interceptor<Annotation>>> interceptorCache = new WeakHashMap<Method, Map<Annotation, Interceptor<Annotation>>>();

	private UpdatableInjectionContext injectionContext;
	private BinderRegistry binderRegistry;
	private FilterRegistry filters;

	public ControllerRouteResolver(UpdatableInjectionContext injectionContext, FilterRegistry filters, BinderRegistry binderRegistry) {
		this.injectionContext = injectionContext;
		this.binderRegistry = binderRegistry;
		this.filters = filters;
	}

	@Override
	public Object resolve(Controller action, com.threewks.thundr.route.HttpMethod method, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) throws RouteResolverException {
		Object controller = getOrCreateController(action);
		Map<Annotation, Interceptor<Annotation>> interceptors = getInterceptors(action);
		Object result = beforeFilters(method, req, resp);
		try {
			result = beforeInterceptors(interceptors, req, resp, result);
			result = invokeAction(action, req, resp, pathVars, controller, result);
			result = afterInterceptors(result, interceptors, req, resp);
			result = afterFilters(method, req, resp, result);
		} catch (Exception e) {
			result = exceptionInterceptors(interceptors, req, resp, e);
			result = exceptionFilters(method, req, resp, e, result);
			if (result == null) {
				throw new RouteResolverException(e, "Failed in %s: %s", action, e.getMessage());
			}
		}
		Logger.debug("%s -> %s resolved", req.getRequestURI(), action);
		return result;
	}

	private Object invokeAction(Controller action, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars, Object controller, Object existingResult) throws Exception {
		if (existingResult != null) {
			return existingResult;
		}
		try {
			List<?> arguments = bindArguments(action, req, resp, pathVars);
			return action.invoke(controller, arguments);
		} catch (InvocationTargetException e) {
			// we need to unwrap InvocationTargetExceptions to get at the real exception
			Exception exception = Cast.as(e.getTargetException(), Exception.class);
			throw exception == null ? new BaseException(e) : exception;
		}
	}

	private Object beforeFilters(com.threewks.thundr.route.HttpMethod method, HttpServletRequest req, HttpServletResponse resp) {
		return filters == null ? null : filters.before(method, req, resp);
	}

	private Object afterFilters(com.threewks.thundr.route.HttpMethod method, HttpServletRequest req, HttpServletResponse resp, Object existingResult) {
		if (filters != null) {
			Object result = filters.after(method, existingResult, req, resp);
			if (result != null) {
				return result;
			}
		}
		return existingResult;
	}

	private Object exceptionFilters(com.threewks.thundr.route.HttpMethod method, HttpServletRequest req, HttpServletResponse resp, Exception exception, Object existingResult) {
		if (filters != null) {
			Object view = filters.exception(method, exception, req, resp);
			if (view != null) {
				return view;
			}
		}
		return existingResult;
	}

	private Map<Annotation, Interceptor<Annotation>> getInterceptors(Controller action) {
		Method method = action.method();
		Map<Annotation, Interceptor<Annotation>> results = interceptorCache.get(method);
		if (results == null) {
			results = findInterceptors(method);
			interceptorCache.put(method, results);
		}
		return results;
	}

	List<Object> bindArguments(Controller action, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) {
		Map<ParameterDescription, Object> boundParameters = new LinkedHashMap<ParameterDescription, Object>();
		for (ParameterDescription parameterDescription : action.parameters()) {
			boundParameters.put(parameterDescription, null);
		}
		if (!boundParameters.isEmpty()) {
			for (Binder binder : binderRegistry.getRegisteredBinders()) {
				binder.bindAll(boundParameters, req, resp, pathVars);
			}
		}
		return new ArrayList<Object>(boundParameters.values());
	}

	private Object afterInterceptors(Object result, Map<Annotation, Interceptor<Annotation>> interceptors, HttpServletRequest req, HttpServletResponse resp) {
		for (Map.Entry<Annotation, Interceptor<Annotation>> interceptorEntry : interceptors.entrySet()) {
			Object interceptorResult = interceptorEntry.getValue().after(interceptorEntry.getKey(), result, req, resp);
			if (interceptorResult != null) {
				return interceptorResult;
			}
		}

		return result;
	}

	private Object exceptionInterceptors(Map<Annotation, Interceptor<Annotation>> interceptors, HttpServletRequest req, HttpServletResponse resp, Exception e) {
		for (Map.Entry<Annotation, Interceptor<Annotation>> interceptorEntry : interceptors.entrySet()) {
			Object interceptorResult = interceptorEntry.getValue().exception(interceptorEntry.getKey(), e, req, resp);
			if (interceptorResult != null) {
				return interceptorResult;
			}
		}
		return null;
	}

	private Object beforeInterceptors(Map<Annotation, Interceptor<Annotation>> interceptors, HttpServletRequest req, HttpServletResponse resp, Object existingResult) {
		if (existingResult != null) {
			return existingResult;
		}
		for (Map.Entry<Annotation, Interceptor<Annotation>> interceptorEntry : interceptors.entrySet()) {
			Object interceptorResult = interceptorEntry.getValue().before(interceptorEntry.getKey(), req, resp);
			if (interceptorResult != null) {
				return interceptorResult;
			}
		}
		return null;
	}

	private Object getOrCreateController(Controller methodAction) {
		Object controller = controllerInstances.get(methodAction.type());
		if (controller == null) {
			synchronized (controllerInstances) {
				controller = controllerInstances.get(methodAction.type());
				if (controller == null) {
					controller = createController(methodAction);
					controllerInstances.put(methodAction.type(), controller);
				}
			}
		}
		return controller;
	}

	<T> T createController(Controller actionMethod) {
		Class<T> type = actionMethod.type();
		if (!injectionContext.contains(type)) {
			injectionContext.inject(type).as(type);
		}
		try {
			return injectionContext.get(type);
		} catch (Exception e) {
			throw new RouteResolverException(e, "Failed to create controller %s: %s", type.toString(), e.getMessage());
		}
	}

	Map<Annotation, Interceptor<Annotation>> findInterceptors(Method method) {
		Map<Annotation, Interceptor<Annotation>> interceptors = new LinkedHashMap<Annotation, Interceptor<Annotation>>();
		for (Annotation annotation : method.getDeclaredAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			Interceptor<Annotation> actionInterceptor = interceptor(annotationType);
			if (actionInterceptor != null) {
				interceptors.put(annotation, actionInterceptor);
			}
		}

		return interceptors;
	}

	@Override
	public <A extends Annotation> void registerInterceptor(Class<A> annotation, Interceptor<A> interceptor) {
		actionInterceptors.put(annotation, interceptor);
		Logger.info("Added ActionInterceptor %s for methods annotated with %s", interceptor, annotation);
	}

	@SuppressWarnings("unchecked")
	public Interceptor<Annotation> interceptor(Class<? extends Annotation> annotationType) {
		return (Interceptor<Annotation>) actionInterceptors.get(annotationType);
	}

	public BinderRegistry getMethodBinderRegistry() {
		return binderRegistry;
	}
}
