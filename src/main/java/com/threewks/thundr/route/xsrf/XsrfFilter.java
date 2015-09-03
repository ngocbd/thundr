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
package com.threewks.thundr.route.xsrf;

import org.apache.commons.lang3.RandomStringUtils;

import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.controller.BaseFilter;

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
 * &lt;form ...&gt;
 *   &lt;input type="hidden" name="XSRF-TOKEN" value="${xsrf}"/&gt;
 *   ...
 * </code>
 */
public class XsrfFilter extends BaseFilter {
	protected String cookieName = "XSRF-TOKEN";
	protected String headerName = Header.XXsrfToken;
	protected String requestPropertyName = "xsrf";

	@Override
	public <T> T before(Request req, Response resp) {
		Cookie cookie = req.getCookie(cookieName);
		String expectedXsrfToken = cookie == null ? null : cookie.getValue();
		if (isANonMutatingRequest(req)) {
			if (expectedXsrfToken == null) {
				// Create an XSRF token, and put it in the cookie
				expectedXsrfToken = createXsrfToken();
				resp.withCookie(Cookie.build(cookieName).withValue(expectedXsrfToken));
			}
		} else {
			String actualXsrfToken = getXsrfTokenFromRequest(req);
			rejectRequestIf(expectedXsrfToken == null, req, "XSRF rejected request on %s %s - XSRF data missing, perform a GET first.", req.getMethod(), req.getRequestPath());
			rejectRequestIf(!expectedXsrfToken.equals(actualXsrfToken), req, "XSRF rejected request on %s %s - invalid '%s' XSRF token", req.getMethod(), req.getRequestPath(), headerName);
		}
		req.putData(requestPropertyName, expectedXsrfToken);
		return null;
	}

	private boolean isANonMutatingRequest(Request req) {
		return req.isA(HttpMethod.GET) || req.isA(HttpMethod.HEAD) || req.isA(HttpMethod.OPTIONS);
	}

	public String getXsrfTokenFromRequest(Request req) {
		String actualXsrfToken = req.getHeader(headerName);
		if (actualXsrfToken == null) {
			actualXsrfToken = req.getParameter(cookieName);
		}
		return actualXsrfToken;
	}

	private void rejectRequestIf(boolean reject, Request req, String format, Object... args) {
		if (reject) {
			rejectRequest(req, format, args);
		}
	}

	/**
	 * Override to customise behaviour on rejection of requests - for example to change logging or
	 * write a record to a DB etc.
	 * 
	 * @param format
	 * @param args
	 */
	public void rejectRequest(Request req, String format, Object... args) {
		Logger.warn(format, args);
		throw new HttpStatusException(StatusCode.Forbidden, format, args);
	}

	protected String createXsrfToken() {
		return RandomStringUtils.randomAlphanumeric(32);
	}
}
