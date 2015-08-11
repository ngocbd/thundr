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

import com.threewks.thundr.injection.Module;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

/**
 * Filters allow a before, after and exception cutpoint on controller methods invoked based on the
 * request url.
 * 
 * They can be added in your {@link Module} by calling {@link FilterRegistry#add(String, Filter)}.
 */
public interface Filter {
	/**
	 * Invoked before the controller method is called, and before binding happens for the controller method. Returning null from this method allows normal execution to continue,
	 * returning anything else results in the controller not being invoked and the returned view being resolved.
	 * 
	 * @param req
	 * @param resp
	 * @return the view to resolve if this filter wishes to resolve the view and prevent the controller being invoked, null otherwise
	 */
	public <T> T before(Request req, Response resp);

	/**
	 * Invoked after the controller method is called. Returning null from this method will result in the given view object being resolved as normal, any other
	 * return value will be resolved as the view instead of the controller's returned view.
	 * 
	 * @param view the view the controller returned after execution
	 * @param req
	 * @param resp
	 * @return the view to resolve instead of the controllers result, or null to use the result of the controller execution
	 */
	public <T> T after(Object view, Request req, Response resp);

	/**
	 * Invoked if the controller method execution throws an exception. This method can return a view which will be resolved instead of the exception, or null
	 * if the exception should be resolved to a view normally.
	 * 
	 * @param e the exception thrown from the controller method
	 * @param req
	 * @param resp
	 * @return the view to resolve instead of the exception, or null to resolve as normal
	 */
	public <T> T exception(Exception e, Request req, Response resp);
}
