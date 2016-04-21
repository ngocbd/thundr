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
package com.threewks.thundr.route.cors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.controller.Filter;

/**
 * This filter allows CORS requests to be served by this application.
 *
 * A good article on CORS can be found <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS">here</a>.
 * 
 * Any request this filter applies to will have appropriate headers added to permit CORS to work.
 * The extent to which CORS is locked down is controlled by the parameters passed at construction time:
 * <ul>
 * <li>Origins: a list of domains which are permitted access using cors. If null is provided, all domains are permitted (i.e. '*'). Domains should not have a protocol prefixed, all protocols will be
 * accepted.</li>
 * <li>AllowHeaders: a list of headers which are permitted to be submitted. If null is provided, all requested headers will be permitted. Headers are case insensitive.</li>
 * <li>ExposeHeaders: a list of headers which are exposed to the client. If null is provided, no response headers will be permitted. Headers are case insensitive.</li>
 * <li>WithCredentials: if set to false, Access-Control-Allow-Credentials will not be set (and cookies and basic auth will not be transmitted by clients). Defaults to true.</li>
 * </ul>
 * 
 * <strong>Note: For this filter to work, a controller route must be defined which accepts {@link HttpMethod#OPTIONS} requests for the path matching the filter. This is because
 * filters only run when controllers are being invoked.</strong>
 */
public class CorsFilter implements Filter {
	protected List<String> origins;
	protected List<String> allowHeaders;
	protected List<String> exposeHeaders;
	protected boolean withCredentials;

	/**
	 * Creates a filter permitting only the given domains, only the given headers and allows credentials to be passed and only allows credentials
	 * when withCredentials is true
	 * 
	 * @param origins Only the origins specified are permitted to make requests to this server by the client - you can pass "*" to allow all origins
	 * @param allowHeaders Only the set of headers specified here can be transmitted by the client to the server during a CORS request, null for all requested headers
	 * @param exposeHeaders The client can only read these headers from a CORS response
	 * @param withCredentials controls if credentials can be sent in a CORS request (cookies, credentials during basic auth etc)
	 */
	public CorsFilter(List<String> origins, List<String> allowHeaders, List<String> exposeHeaders, boolean withCredentials) {
		super();
		this.origins = origins;
		this.allowHeaders = allowHeaders == null ? null : lowerCaseAll.from(allowHeaders);
		this.exposeHeaders = exposeHeaders == null ? null : lowerCaseAll.from(exposeHeaders);
		this.withCredentials = withCredentials;
	}

	@Override
	public <T> T before(Request req, Response resp) {
		String origin = origins == null ? "*" : origin(req, origins);
		resp.withHeader(Header.Vary, StringUtils.join(vary(), ", "));
		if (origin != null) {
			List<String> allowHeaders = determineAllowedHeaders(this.allowHeaders, req);
			String requestedMethod = req.getHeader(Header.AccessControlRequestMethod);
			// @formatter:off
			resp.withHeader(Header.AccessControlAllowOrigin, origin)
				.withHeader(Header.AccessControlAllowCredentials, "true", withCredentials)
				.withHeader(Header.AccessControlAllowHeaders, StringUtils.join(allowHeaders, ", "), Expressive.isNotEmpty(allowHeaders))
				.withHeader(Header.AccessControlExposeHeaders, StringUtils.join(exposeHeaders, ", "), Expressive.isNotEmpty(exposeHeaders))
				.withHeader(Header.AccessControlAllowMethods, requestedMethod, StringUtils.isNotBlank(requestedMethod));
			// @formatter:on
		}
		return null;
	}

	@Override
	public <T> T after(Object view, Request req, Response resp) {
		return null;
	}

	@Override
	public <T> T exception(Exception e, Request req, Response resp) {
		return null;
	}

	/**
	 * Controls the basic vary header which ensures that any cors specific headers a cache controlled correctly.
	 * 
	 * @return
	 */
	protected List<String> vary() {
		return Arrays.asList(Header.Origin, Header.AccessControlRequestMethod, Header.AccessControlRequestHeaders);
	}

	protected List<String> determineAllowedHeaders(List<String> headers, Request req) {
		List<String> allowedHeaders = new ArrayList<String>();
		List<String> requestedHeaders = req.getHeaders(Header.AccessControlRequestHeaders);
		if (requestedHeaders != null) {
			for (String header : requestedHeaders) {
				String lowercase = StringUtils.lowerCase(header);
				String[] uncombined = StringUtils.split(lowercase, ", ");
				allowedHeaders.addAll(Arrays.asList(uncombined));
			}

			if (headers != null) {
				allowedHeaders.retainAll(headers);
			}
		}
		return allowedHeaders;
	}

	/**
	 * Examines the request for the Origin header. If the origin header is
	 * present (ignoring protocol) in the set of supported origins, the header
	 * value will be returned (signifying permission), otherwise null is returned.
	 * 
	 * @param req
	 * @param origins
	 * @return
	 */
	protected String origin(Request req, List<String> origins) {
		String originHeader = req.getHeader(Header.Origin);
		String origin = StringUtils.substringAfter(originHeader, "//");
		String result = origins.contains(origin) ? originHeader : null;
		if (result == null) {
			result = origins.contains("*") ? "*" : null;
		}
		return result;
	}

	private static final ETransformer<String, String> lowerCase = new ETransformer<String, String>() {
		@Override
		public String from(String from) {
			return StringUtils.lowerCase(from);
		}
	};
	private static final CollectionTransformer<String, String> lowerCaseAll = Expressive.Transformers.transformAllUsing(lowerCase);

	protected List<String> getOrigins() {
		return origins;
	}

	protected List<String> getHeaders() {
		return allowHeaders;
	}

	protected boolean isWithCredentials() {
		return withCredentials;
	}
}
