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
package com.threewks.thundr.route.xsrf;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.controller.BaseInterceptor;

/**
 * XSRF/CSRF protection using the <a href="https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet#General_Recommendation:_Synchronizer_Token_Pattern">Synchronizer
 * Token Pattern</a>
 *
 * Requests that do not mutate application state (GET, HEAD, OPTIONS) result in an XSRF cookie with a random value is set, if not present.
 * 
 * Requests that do mutate application state (POST, PUT, PATCH, DELETE) must send this cookie,
 * and provide that same value by another means (as a header for ajax requests, or a post parameter for non-ajax requests).
 * If the two values do not match or are not present, a 403 is returned.
 * 
 * The value of the token is placed in the request data as 'xsrf', making it available for
 * use in page templates (for example to include as a hidden field: <code>
 * &ltform ...%gt;
 *   &ltinput type="hidden" name="XSRF-TOKEN" value="${xsrf}"/&gt;
 *   ...
 * </code>
 * 
 * @see XsrfFilter
 */
public class XsrfInterceptor extends BaseInterceptor<Xsrf> {
	protected XsrfFilter delegate = new XsrfFilter();

	@Override
	public <T> T before(Xsrf annotation, Request req, Response resp) {
		return delegate.before(req, resp);
	}
}
