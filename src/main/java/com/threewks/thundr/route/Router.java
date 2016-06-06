/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.route;

import static com.atomicleopard.expressive.Expressive.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.controller.Controller;

public class Router {
	private Map<Route, RouteResult> actionsForRoutes = new HashMap<Route, RouteResult>();

	private Map<HttpMethod, Map<String, Route>> routes = createRoutesMap();
	private Map<String, Route> namedRoutes = new HashMap<String, Route>();

	private Map<Class<? extends RouteResult>, RouteResolver<?>> actionResolvers = new LinkedHashMap<Class<? extends RouteResult>, RouteResolver<?>>();

	private boolean debug = false;

	public Router get(String route, Class<?> controller, String controllerMethod) {
		return this.get(route, controller, controllerMethod, null);
	}

	public Router get(String route, Class<?> controller, String controllerMethod, String name) {
		return this.add(HttpMethod.GET, route, controller, controllerMethod, name);
	}

	public <T extends RouteResult> Router get(String route, T action, String name) {
		return this.add(HttpMethod.GET, route, action, name);
	}

	public <T extends RouteResult> Router get(String route, T action) {
		return this.get(route, action, null);
	}

	public Router put(String route, Class<?> controller, String controllerMethod) {
		return this.put(route, controller, controllerMethod, null);
	}

	public Router put(String route, Class<?> controller, String controllerMethod, String name) {
		return this.add(HttpMethod.PUT, route, controller, controllerMethod, name);
	}

	public <T extends RouteResult> Router put(String route, T action) {
		return this.put(route, action, null);
	}

	public <T extends RouteResult> Router put(String route, T action, String name) {
		return this.add(HttpMethod.PUT, route, action, name);
	}

	public Router post(String route, Class<?> controller, String controllerMethod) {
		return this.post(route, controller, controllerMethod, null);
	}

	public Router post(String route, Class<?> controller, String controllerMethod, String name) {
		return this.add(HttpMethod.POST, route, controller, controllerMethod, name);
	}

	public <T extends RouteResult> Router post(String route, T action) {
		return this.post(route, action, null);
	}

	public <T extends RouteResult> Router post(String route, T action, String name) {
		return this.add(HttpMethod.POST, route, action, name);
	}

	public Router delete(String route, Class<?> controller, String method) {
		return this.delete(route, controller, method, null);
	}

	public Router delete(String route, Class<?> controller, String controllerMethod, String name) {
		return this.add(HttpMethod.DELETE, route, controller, controllerMethod, name);
	}

	public <T extends RouteResult> Router delete(String route, T action) {
		return this.delete(route, action, null);
	}

	public <T extends RouteResult> Router delete(String route, T action, String name) {
		return this.add(HttpMethod.DELETE, route, action, name);
	}

	public Router add(HttpMethod method, String route, Class<?> controller, String controllerMethod, String name) {
		return this.add(method, route, new Controller(controller, controllerMethod), name);
	}

	/**
	 * Removes any previously registered route of the given method which matches the given route exactly.
	 * The route is matched as a string, so any path variables must be exact.
	 * 
	 * @param method
	 * @param route
	 * @return
	 */
	public Router remove(HttpMethod method, String route) {
		remove(getRoute(method, route));
		return this;
	}

	/**
	 * Removes any previously registered route with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Router remove(String name) {
		remove(namedRoutes.get(name));
		return this;
	}

	/**
	 * Returns true is a route has been registered for the given method and the given route exactly.
	 * The route is matched as a string, so any path variables must be exact.
	 * 
	 * @param method
	 * @param route
	 * @return
	 */
	public boolean has(HttpMethod method, String route) {
		return getRoute(method, route) != null;
	}

	public boolean has(String name) {
		return getNamedRoute(name) != null;
	}

	public boolean isEmpty() {
		return actionsForRoutes.isEmpty();
	}

	public <T extends RouteResult> Router add(HttpMethod httpMethod, String routePath, T action, String name) {
		Route route = new Route(httpMethod, routePath, name);
		String path = route.getRouteMatchRegex();
		Map<String, Route> routesForMethod = this.routes.get(httpMethod);
		if (routesForMethod.containsKey(path)) {
			Route existingRoute = routesForMethod.get(path);
			throw new RouteException("Unable to add the route '%s %s' - the route '%s %s' already exists which matches the same pattern. To override, you can remove the existing route first.",
					route.getMethod(), route.getRoute(), existingRoute.getMethod(), existingRoute.getRoute());
		}
		if (StringUtils.isNotBlank(name)) {
			if (namedRoutes.containsKey(name)) {
				Route existingRoute = namedRoutes.get(name);
				throw new RouteException(
						"Unable to add the route '%s %s' with the name '%s' - the route '%s %s' has already been registered with this name. To override, you can remove the named route first.",
						route.getMethod(), route.getRoute(), name, existingRoute.getMethod(), existingRoute.getRoute());
			}
			this.namedRoutes.put(name, route);
		}
		routesForMethod.put(path, route);
		this.actionsForRoutes.put(route, action);
		return this;
	}

	public Route getNamedRoute(String name) {
		return namedRoutes.get(name);
	}

	public ReverseRoute getReverseRoute(Map<String, Object> pathVars, String name) {
		Route namedRoute = getNamedRoute(name);
		return namedRoute.getReverseRoute(pathVars);
	}

	public List<ReverseRoute> getReverseRoutes(Map<String, Object> pathVars, String... names) {
		List<ReverseRoute> results = new ArrayList<>();
		for (String name : names) {
			Route route = namedRoutes.get(name);
			if (route != null) {
				try {
					results.add(route.getReverseRoute(pathVars));
				} catch (ReverseRouteException e) {
					// swallow, nom nom nom
				}
			}
		}
		return results;
	}

	public Object resolve(Request req, Response resp) {
		Logger.debug("Request %s: %s %s", req.getId(), req.getMethod(), req.getRequestPath());
		Route route = req.getRoute();
		if (route != null) {
			RouteResult action = actionsForRoutes.get(route);
			return resolve(action, req, resp);
		}
		String debugString = debug ? listRoutes() : "";
		throw new RouteNotFoundException("No route matching the request %s %s\n%s", req.getMethod(), req.getRequestPath(), debugString);
	}

	public Route findMatchingRoute(HttpMethod method, String routePath) {
		Map<String, Route> routesForMethod = routes.get(method);
		for (Route route : routesForMethod.values()) {
			if (route.matches(routePath)) {
				return route;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends RouteResult> Object resolve(final T action, final Request req, final Response resp) {
		RouteResolver<T> actionResolver = (RouteResolver<T>) actionResolvers.get(action.getClass());
		return actionResolver.resolve(action, req, resp);
	}

	private static final String routeDisplayFormat = "%s: %s\n";

	public String listRoutes() {
		Set<String> allRoutes = new HashSet<String>();
		for (Map<String, Route> routeEntries : routes.values()) {
			allRoutes.addAll(routeEntries.keySet());
		}
		List<String> allRouteNames = list(allRoutes);
		Collections.sort(allRouteNames);

		StringBuilder sb = new StringBuilder();
		for (String route : allRouteNames) {
			for (HttpMethod method : HttpMethod.all()) {
				Map<String, Route> routesForType = routes.get(method);
				if (routesForType.containsKey(route)) {
					Route actualRoute = routesForType.get(route);
					RouteResult action = this.actionsForRoutes.get(actualRoute);
					sb.append(String.format(routeDisplayFormat, actualRoute, action));
				}
			}
		}
		return sb.toString();
	}

	public <A extends RouteResult> void addResolver(Class<A> actionType, RouteResolver<A> actionResolver) {
		actionResolvers.put(actionType, actionResolver);
		Logger.debug("Added action resolver %s for actions of type %s", actionResolver.getClass().getSimpleName(), actionType);
	}

	@SuppressWarnings("unchecked")
	public <A extends RouteResult> RouteResolver<A> getResolver(Class<A> actionType) {
		return (RouteResolver<A>) actionResolvers.get(actionType);
	}

	private Map<HttpMethod, Map<String, Route>> createRoutesMap() {
		Map<HttpMethod, Map<String, Route>> routesMap = new HashMap<HttpMethod, Map<String, Route>>();
		for (HttpMethod type : HttpMethod.all()) {
			routesMap.put(type, new LinkedHashMap<String, Route>());
		}
		return routesMap;
	}

	private void remove(Route route) {
		if (route != null) {
			Map<String, Route> routesForMethod = this.routes.get(route.getMethod());
			routesForMethod.remove(route.getRouteMatchRegex());
			actionsForRoutes.remove(route);
			if (route.getName() != null) {
				namedRoutes.remove(route.getName());
			}
		}
	}

	private Route getRoute(HttpMethod method, String route) {
		Map<String, Route> routesForMethod = this.routes.get(method);
		if (routesForMethod != null) {
			for (Route potentialRoute : routesForMethod.values()) {
				if (potentialRoute.getRoute().equals(route)) {
					return potentialRoute;
				}
			}
		}
		return null;
	}

}
