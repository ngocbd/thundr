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
package com.threewks.thundr.route.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.atomicleopard.expressive.collection.Pair;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.Route;

/**
 * {@link Filter} allows the definition of a simple interceptor strategy for invocation of controller methods
 * which are being invoked on a certain path.
 * 
 * They are an alternative to an {@link Interceptor}, which allows you to target an interceptor for a particular
 * controller method.
 * 
 * Filters are registered with this class at startup.
 */
public class FilterRegistryImpl implements FilterRegistry {
	private List<Pair<String, Filter>> filters = new ArrayList<>();

	/**
	 * Add the given filter for all controller methods on the given path.
	 * Allows wildcards in the form of * and **.
	 * e.g. <code>/**</code> - all methods <code>/path/**</code> any methods invoked on /path/ and any number of subpaths <code>/path/*</code> any methods invoked on an subpath of path.
	 * 
	 * @param filter
	 * @param paths
	 */
	@Override
	public void add(Filter filter, String... paths) {
		for (String path : paths) {
			String regex = convertPathStringToRegex(path);
			filters.add(new Pair<>(regex, filter));
		}
	}

	/**
	 * Remove the given filter from the given path.
	 * 
	 * @param filter
	 * @param paths
	 */
	@Override
	public void remove(Filter filter, String... paths) {
		List<Pair<String, Filter>> toRemove = new ArrayList<>();
		for (String path : paths) {
			String regex = convertPathStringToRegex(path);
			Pair<String, Filter> pair = new Pair<String, Filter>(regex, filter);
			toRemove.add(pair);
		}
		filters.removeAll(toRemove);
	}

	/**
	 * Remove the given filter from any paths it was previously added for
	 * 
	 * @param filter
	 */
	@Override
	public void remove(Filter filter) {
		List<Pair<String, Filter>> toRemove = new ArrayList<>();
		for (Pair<String, Filter> pair : filters) {
			if (pair.getB().equals(filter)) {
				toRemove.add(pair);
			}
		}
		filters.removeAll(toRemove);
	}

	@Override
	public void remove(Class<? extends Filter> filter) {
		List<Pair<String, Filter>> toRemove = new ArrayList<>();
		for (Pair<String, Filter> pair : filters) {
			if (filter.equals(pair.getB().getClass())) {
				toRemove.add(pair);
			}
		}
		filters.removeAll(toRemove);
	}

	@Override
	public void remove(Class<? extends Filter> filter, String... paths) {
		List<Pair<String, Filter>> toRemove = new ArrayList<>();
		for (String path : paths) {
			String regex = convertPathStringToRegex(path);
			for (Pair<String, Filter> pair : filters) {
				if (filter.equals(pair.getB().getClass()) && pair.getA().equals(regex)) {
					toRemove.add(pair);
				}
			}
		}
		filters.removeAll(toRemove);
	}

	/**
	 * @param filter
	 * @param path
	 * @return true if the given filter has already been added on the given path
	 */
	@Override
	public boolean has(Filter filter, String path) {
		String regex = convertPathStringToRegex(path);
		Pair<String, Filter> pair = new Pair<String, Filter>(regex, filter);
		return filters.contains(pair);
	}

	@Override
	public boolean has(Class<? extends Filter> filter, String path) {
		String regex = convertPathStringToRegex(path);
		for (Pair<String, Filter> pair : filters) {
			if (pair.getA().equals(regex) && pair.getB().getClass().equals(filter)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param req
	 * @param resp
	 * @return a view to use instead of calling through to the controller method, or null if execution should continue
	 */
	@Override
	public Object before(Request req, Response resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getRequestPath());
		for (Filter filter : matchingFilters) {
			Object result = filter.before(req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param view
	 * @param req
	 * @param resp
	 * @return a view to use instead of the result from the controller method, or null if the given result should be used
	 */
	@Override
	public Object after(Object view, Request req, Response resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getRequestPath());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.after(view, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param e
	 * @param req
	 * @param resp
	 * @return a view to use instead of allowing the exception to propagate up, or null to continue exception flow as normal
	 */
	@Override
	public Object exception(Exception e, Request req, Response resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getRequestPath());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.exception(e, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private List<Filter> findMatchingFilters(String path) {
		List<Filter> filters = new ArrayList<Filter>();
		if (path != null) {
			for (Pair<String, Filter> entry : this.filters) {
				if (path.matches(entry.getA())) {
					filters.add(entry.getB());
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
