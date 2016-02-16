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
 * <li>Headers: a list of headers which are permitted. If null is provided, all requested headers will be permitted. Headers are case insensitive.</li>
 * <li>WithCredentials: if set to false, Access-Control-Allow-Credentials will not be set (and cookies and basic auth will not be transmitted by clients). Defaults to true.</li>
 * </ul>
 * 
 * <strong>Note: For this filter to work, a controller route must be defined which accepts {@link HttpMethod#OPTIONS} requests for the path matching the filter. This is because
 * filters only run when controllers are being invoked.</strong>
 */
// TODO - v3 - Needs to include 'Access-Control-Expose-Headers'
// TODO - v3 - Should default to permit nothing, not permit everything
public class CorsFilter implements Filter {
	private final List<String> origins;
	private final List<String> headers;
	private boolean withCredentials;

	/**
	 * Creates a filter permitting all domains, any headers and allows credentials to be passed.
	 */
	public CorsFilter() {
		this(null, null);
	}

	/**
	 * Creates a filter permitting only the given domains, any headers and allows credentials to be if one or more domains are specified.
	 * 
	 * @param origins
	 */
	public CorsFilter(List<String> origins) {
		this(origins, null);
	}

	/**
	 * Creates a filter permitting only the given domains, only the given headers and allows credentials to be if one or more domains are specified.
	 * 
	 * @param origins
	 * @param headers
	 */
	public CorsFilter(List<String> origins, List<String> headers) {
		this(origins, headers, origins != null);
	}

	/**
	 * Creates a filter permitting only the given domains, only the given headers and allows credentials to be passed and only allows credentials
	 * when withCredentials is true
	 * 
	 * @param origins
	 * @param headers
	 * @param withCredentials
	 */
	public CorsFilter(List<String> origins, List<String> headers, boolean withCredentials) {
		super();
		this.origins = origins;
		this.headers = headers == null ? null : lowerCaseAll.from(headers);
		this.withCredentials = withCredentials;
	}

	@Override
	public <T> T before(Request req, Response resp) {
		String origin = origins == null ? "*" : origin(req, origins);
		resp.withHeader(Header.Vary, StringUtils.join(vary(), ", "));
		if (origin != null) {
			List<String> allowedHeaders = determineAllowedHeaders(headers, req);
			String requestedMethod = req.getHeader(Header.AccessControlRequestMethod);
			// @formatter:off
			resp.withHeader(Header.AccessControlAllowOrigin, origin)
				.withHeader(Header.AccessControlAllowCredentials, "true", withCredentials)
				.withHeader(Header.AccessControlAllowHeaders, StringUtils.join(allowedHeaders, ", "), Expressive.isNotEmpty(allowedHeaders))
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
		return origins.contains(origin) ? originHeader : null;
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
		return headers;
	}

	protected boolean isWithCredentials() {
		return withCredentials;
	}
}
