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
package com.threewks.thundr.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link RequestThreadLocal} retains the {@link HttpServletRequest} and {@link HttpServletResponse} in a {@link ThreadLocal} whose lifecycle is that of the request.
 * 
 * This is useful for classes that require the {@link HttpServletRequest} or {@link HttpServletResponse} but
 * whose call stack is a long way from a controller method. In general, applications should prefer
 * to pass the request or response down as method arguments, however in circumstances when this
 * causes excessive coupling, it may be preferable to use the {@link RequestThreadLocal} class.
 */
public class RequestThreadLocal {
	private static ThreadLocal<HttpServletRequest> req = new ThreadLocal<HttpServletRequest>();
	private static ThreadLocal<HttpServletResponse> resp = new ThreadLocal<HttpServletResponse>();

	/**
	 * @return the {@link HttpServletRequest} for the current request. Will be null if this is called outside of
	 *         the context of a serlvet request.
	 */
	public static HttpServletRequest getRequest() {
		return req.get();
	}

	/**
	 * 
	 * * @return the {@link HttpServletResponse} for the current request. Will be null if this is called outside of
	 * the context of a serlvet request.
	 */
	public static HttpServletResponse getResponse() {
		return resp.get();
	}

	/**
	 * Sets both the request and response for the current request. Application code should never need to call this
	 * directly.
	 * 
	 * @param req
	 * @param resp
	 */
	public static void set(HttpServletRequest req, HttpServletResponse resp) {
		RequestThreadLocal.req.set(req);
		RequestThreadLocal.resp.set(resp);
	}

	/**
	 * Clears both the request and response for the current request. Application code should never need to call this
	 * directly.
	 */
	public static void clear() {
		RequestThreadLocal.set(null, null);
	}
}
