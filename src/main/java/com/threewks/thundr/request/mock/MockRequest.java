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
package com.threewks.thundr.request.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.request.BaseRequest;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.route.HttpMethod;

public class MockRequest extends BaseRequest implements Request {
	private static final ETransformer<Collection<Cookie>, Map<String, List<Cookie>>> ToCookieLookup = Expressive.Transformers.toBeanLookup("name", Cookie.class);
	private String path;
	private String contentType;
	private Map<String, List<String>> headers = new LinkedHashMap<>();
	private Map<String, List<String>> parameters = new LinkedHashMap<>();
	private Map<String, Object> data = new LinkedHashMap<>();
	private Object rawRequest;
	private List<Cookie> cookies = new ArrayList<>();
	private String encoding = "UTF-8";
	private String content;

	public MockRequest() {
		this(HttpMethod.GET, "/path");
	}

	public MockRequest(HttpMethod httpMethod, String path) {
		super(httpMethod);
		this.path = path;
	}

	public MockRequest withRawRequest(Object raw) {
		this.rawRequest = raw;
		return this;
	}

	@Override
	public String getContentTypeString() {
		return contentType;
	}

	@Override
	public String getCharacterEncoding() {

		return encoding;
	}

	@Override
	public String getHeader(String name) {
		List<String> list = headers.get(name);
		return list == null ? null : list.get(0);
	}

	@Override
	public List<String> getHeaders(String name) {
		return headers.get(name);
	}

	@Override
	public Map<String, List<String>> getAllHeaders() {
		return headers;
	}

	@Override
	public String getParameter(String name) {
		List<String> list = parameters.get(name);
		return list == null ? null : list.get(0);
	}

	@Override
	public List<String> getParameters(String name) {
		return parameters.get(name);
	}

	@Override
	public Map<String, List<String>> getAllParameters() {
		return parameters;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getRequestPath() {
		return path;
	}

	@Override
	public Cookie getCookie(String name) {
		for (Cookie cookie : this.cookies) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	@Override
	public Map<String, List<Cookie>> getAllCookies() {
		return ToCookieLookup.from(this.cookies);
	}

	@Override
	public Reader getReader() {
		return content == null ? null : new StringReader(content);
	}

	@Override
	public InputStream getInputStream() {
		try {
			return new ByteArrayInputStream(content.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new BaseException(e);
		}
	}

	@Override
	protected Object getRawRequest() {
		return rawRequest;
	}

	public MockRequest withUrl(String path) {
		this.path = path;
		return this;
	}

	public MockRequest withMethod(HttpMethod method) {
		this.httpMethod = method;
		return this;
	}

	public MockRequest withHeader(String key, String value) {
		List<String> list = this.headers.get(key);
		if (list == null) {
			this.headers.put(key, Expressive.<String> list(value));
		} else {
			list.add(value);
		}
		return this;
	}

	public MockRequest withHeader(String key, String... values) {
		for (String value : values) {
			withHeader(key, value);
		}
		return this;
	}

	public MockRequest replaceHeader(String key, String value) {
		this.headers.put(key, Expressive.<String> list(value));
		return this;
	}

	public MockRequest withContentType(ContentType contentType) {
		this.contentType = contentType.value();
		return this;
	}

	public MockRequest withContentTypeString(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public MockRequest withParameter(String key, String value) {
		List<String> list = this.parameters.get(key);
		if (list == null) {
			this.parameters.put(key, Expressive.list(value));
		} else {
			list.add(value);
		}
		return this;
	}

	public MockRequest withRequestPath(String path) {
		this.path = path;
		return this;
	}

	public MockRequest withCookie(String name, String value) {
		return withCookie(Cookie.build(name).withValue(value).build());
	}

	public MockRequest withCookie(Cookie cookie) {
		this.cookies.add(cookie);
		return this;
	}

	public MockRequest withBody(String body) {
		this.content = body;
		return this;
	}

	@Override
	public void putData(String key, Object value) {
		this.data.put(key, value);
	}

	@Override
	public void putData(Map<String, Object> values) {
		this.data.putAll(values);
	}

	@Override
	public Map<String, Object> getAllData() {
		return this.data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getData(String key) {
		return (T) this.data.get(key);
	}

	@Override
	public boolean isSecure() {
		return false;
	}
}
