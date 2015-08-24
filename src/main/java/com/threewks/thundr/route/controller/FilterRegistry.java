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
package com.threewks.thundr.route.controller;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

/**
 * {@link Filter} allows the definition of a simple interceptor strategy for invocation of controller methods
 * which are being invoked on a certain path.
 * 
 * They are an alternative to an {@link Interceptor}, which allows you to target an interceptor for a particular
 * controller method.
 * 
 * Filters are registered with this class at startup.
 */
public interface FilterRegistry {
	/**
	 * Add the given filter for all controller methods on the given path.
	 * Allows wildcards in the form of * and **.
	 * e.g. <code>/**</code> - all methods <code>/path/**</code> any methods invoked on /path/ and any number of subpaths <code>/path/*</code> any methods invoked on an subpath of path.
	 * 
	 * @param path
	 * @param filter
	 */
	public void add(String path, Filter filter);

	/**
	 * Remove the given filter from the given path.
	 * 
	 * @param path
	 * @param filter
	 */
	public void remove(String path, Filter filter);
	/**
	 * Remove filters of the given type from the given path.
	 * 
	 * @param path
	 * @param filter
	 */
	public void remove(String path, Class<? extends Filter> filter);

	/**
	 * Remove the given filter from any paths it was previously added for
	 * 
	 * @param filter
	 */
	public void remove(Filter filter);
	/**
	 * Remove the filters of the given path from any paths previously added
	 * 
	 * @param filter
	 */
	public void remove(Class<? extends Filter> filter);

	/**
	 * @param path
	 * @param filter
	 * @return true if the given filter has already been added on the given path
	 */
	public boolean has(String path, Filter filter);
	
	/**
	 * 
	 * @param path
	 * @param filter
	 * @return true if a filter with the given type has already been added on the given path
	 */
	public boolean has(String path, Class<? extends Filter> filter);

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param req
	 * @param resp
	 * @return a view to use instead of calling through to the controller method, or null if execution should continue
	 */
	public Object before(Request req, Response resp);

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param view
	 * @param req
	 * @param resp
	 * @return a view to use instead of the result from the controller method, or null if the given result should be used
	 */
	public Object after(Object view, Request req, Response resp);

	/**
	 * Used by the framework at runtime, you should not need to invoke this method directly.
	 * 
	 * @param e
	 * @param req
	 * @param resp
	 * @return a view to use instead of allowing the exception to propagate up, or null to continue exception flow as normal
	 */
	public Object exception(Exception e, Request req, Response resp);

}
