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
package com.threewks.thundr.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.action.method.ActionInterceptor;

/**
 * This interface allows the definition of a simple interceptor strategy for invocation of controller methods
 * which are being invoked on a certain path.
 * 
 * They are an alternative to an {@link ActionInterceptor}, which allows you to target an interceptor for a particular
 * controller method.
 */
public class Filters {
	private Map<String, List<Filter>> filters = new HashMap<String, List<Filter>>();

	/**
	 * Add the given filter for all controller methods on the given path.
	 * Allows wildcards in the form of * and **.
	 * e.g. <code>/**</code> - all methods <code>/path/**</code> any methods invoked on /path/ and any number of subpaths <code>/path/*</code> any methods invoked on an subpath of path.
	 * 
	 * @param path
	 * @param filter
	 */
	public void add(String path, Filter filter) {
		String regex = convertPathStringToRegex(path);
		List<Filter> existing = filters.get(regex);
		if (existing == null) {
			existing = new ArrayList<Filter>();
			filters.put(regex, existing);
		}
		existing.add(filter);
	}

	/**
	 * Remove the given filter from the given path.
	 * 
	 * @param path
	 * @param filter
	 */
	public void remove(String path, Filter filter) {
		String regex = convertPathStringToRegex(path);
		List<Filter> existing = filters.get(regex);
		if (existing != null) {
			existing.remove(filter);
		}
	}

	/**
	 * Remove the given filter from any paths it was previously added for
	 * 
	 * @param filter
	 */
	public void remove(Filter filter) {
		for (List<Filter> fs : filters.values()) {
			fs.remove(filter);
		}
	}

	/**
	 * @param path
	 * @param filter
	 * @return true if the given filter has already been added on the given path
	 */
	public boolean has(String path, Filter filter) {
		List<Filter> filtersForPath = filters.get(convertPathStringToRegex(path));
		return filtersForPath == null ? false : filtersForPath.contains(filter);
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param routeType
	 * @param req
	 * @param resp
	 * @return a view to use instead of calling through to the controller method, or null if execution should continue
	 */
	public Object before(RouteType routeType, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getPathInfo());
		for (Filter filter : matchingFilters) {
			Object result = filter.before(routeType, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param routeType
	 * @param view
	 * @param req
	 * @param resp
	 * @return a view to use instead of the result from the controller method, or null if the given result should be used
	 */
	public Object after(RouteType routeType, Object view, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getPathInfo());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.after(routeType, view, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param routeType
	 * @param e
	 * @param req
	 * @param resp
	 * @return a view to use instead of allowing the exception to propagate up, or null to continue exception flow as normal
	 */
	public Object exception(RouteType routeType, Exception e, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getRequestURI());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.exception(routeType, e, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private List<Filter> findMatchingFilters(String path) {
		List<Filter> filters = new ArrayList<Filter>();
		if (path != null) {
			for (Map.Entry<String, List<Filter>> entry : this.filters.entrySet()) {
				String key = entry.getKey();
				if (path.matches(key)) {
					filters.addAll(entry.getValue());
				}
			}
		}
		return filters;
	}

	static String convertPathStringToRegex(String path) {
		String wildCardPlaceholder = "____placeholder____";
		path = path.replaceAll("\\*\\*", wildCardPlaceholder);
		path = path.replaceAll("\\*", Matcher.quoteReplacement("[" + Route.AcceptablePathCharacters + "]*?"));
		path = Route.PathParameterPattern.matcher(path).replaceAll(Matcher.quoteReplacement("([" + Route.AcceptablePathCharacters + "]+)"));
		path = path.replaceAll(wildCardPlaceholder, Matcher.quoteReplacement("[" + Route.AcceptableMultiPathCharacters + "]*?"));
		return path + Route.SemiColonDelimitedRequestParameters;
	}
}
