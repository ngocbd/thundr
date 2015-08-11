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
package com.threewks.thundr.request.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.BaseRequest;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.route.HttpMethod;

public class ServletRequest extends BaseRequest implements Request {
	private HttpServletRequest req;

	public ServletRequest(HttpServletRequest servletRequest, HttpMethod httpMethod) {
		super(httpMethod);
		this.req = servletRequest;
	}

	@Override
	protected Object getRawRequest() {
		return req;
	}

	@Override
	public String getContentTypeString() {
		return req.getContentType();
	}

	@Override
	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public List<String> getHeaders(String name) {
		return Header.getHeaders(name, req);
	}

	@Override
	public Map<String, List<String>> getAllHeaders() {
		return Header.getHeaderMap(req);
	}

	@Override
	public String getParameter(String name) {
		return req.getParameter(name);
	}

	@Override
	public List<String> getParameters(String name) {
		String[] values = req.getParameterValues(name);
		return values == null ? null : Arrays.asList(values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<String>> getAllParameters() {
		Map<String, String[]> parameters = req.getParameterMap();
		Map<String, List<String>> result = new LinkedHashMap<>();
		parameters.forEach((key, value) -> {
			result.put(key, Arrays.asList(value));
		});
		return result;
	}

	@Override
	public int getContentLength() {
		return req.getContentLength();
	}

	@Override
	public String getRequestPath() {
		return req.getRequestURI();
	}

	@Override
	public List<Cookie> getCookies() {
		// @formatter:off
		return Arrays.stream(req.getCookies())
			.map(Servlets::toThundrCookie)
			.collect(Collectors.toList());
		// @formatter:on
	}

	@Override
	public Cookie getCookie(String name) {
		// @formatter:off
		return Arrays.stream(req.getCookies())
				.filter(cookie -> cookie.getName().equalsIgnoreCase(name))
				.findFirst()
				.map(Servlets::toThundrCookie)
				.orElse(null);
		// @formatter:on
	}

	@Override
	public List<Cookie> getCookies(String name) {
		// @formatter:off
		return Arrays.stream(req.getCookies())
				.filter(cookie -> cookie.getName().equalsIgnoreCase(name))
				.map(Servlets::toThundrCookie)
				.collect(Collectors.toList());
		// @formatter:on
	}

	@Override
	public Map<String, List<Cookie>> getAllCookies() {
		// @formatter:off
		return Arrays.stream(req.getCookies())
				.map(Servlets::toThundrCookie)
				.collect(Collectors.groupingBy(Cookie::getName));
		// @formatter:on
	}

	@Override
	public BufferedReader getReader() {
		try {
			return req.getReader();
		} catch (IOException e) {
			throw new BaseException(e);
		}
	}

}
