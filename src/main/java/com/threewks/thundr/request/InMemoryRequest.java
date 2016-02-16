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
package com.threewks.thundr.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.route.HttpMethod;

public class InMemoryRequest extends BaseRequest {
	protected String contentType = ContentType.TextPlain.value();
	protected String characterEncoding = "UTF-8";
	protected Map<String, Object> requestScopeData = new LinkedHashMap<>();
	protected String path;
	protected byte[] requestData;

	public InMemoryRequest() {
		super(null, null);
	}

	public InMemoryRequest(HttpMethod httpMethod, String path) {
		super(httpMethod, null);
		this.path = path;
	}

	public InMemoryRequest(HttpMethod httpMethod, String path, byte[] content, String contentType, String characterEncoding) {
		this(httpMethod, path);
		this.requestData = content;
		this.contentType = contentType;
	}

	public InMemoryRequest(HttpMethod httpMethod, String path, String content, String contentType) {
		this(httpMethod, path, content.getBytes(StandardCharsets.UTF_8), contentType, "UTF-8");
	}

	@Override
	public long getContentLength() {
		return this.requestData == null ? 0 : this.requestData.length;
	}

	@Override
	public String getContentTypeString() {
		return contentType;
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public Reader getReader() {
		try {
			String s = new String(this.requestData, characterEncoding);
			return new StringReader(s);
		} catch (UnsupportedEncodingException e) {
			throw new BaseException(e);
		}
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.requestData);
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public List<String> getHeaders(String name) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, List<String>> getAllHeaders() {
		return Collections.emptyMap();
	}

	@Override
	public String getParameter(String name) {
		return null;
	}

	@Override
	public List<String> getParameters(String name) {
		return null;
	}

	@Override
	public Map<String, List<String>> getAllParameters() {
		return Collections.emptyMap();
	}

	@Override
	public void putData(String key, Object value) {
	}

	@Override
	public void putData(Map<String, Object> values) {
		this.requestScopeData.putAll(values);
	}

	@Override
	public Map<String, Object> getAllData() {
		return new LinkedHashMap<>(this.requestScopeData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getData(String key) {
		return (T) this.requestScopeData.get(key);
	}

	@Override
	public String getRequestPath() {
		return this.path;
	}

	@Override
	public URI getRequestUri() {
		// TODO - test this doesn't throw an exception
		return URI.create(path);
	}

	@Override
	public Cookie getCookie(String name) {
		return null;
	}

	@Override
	public Map<String, List<Cookie>> getAllCookies() {
		return Collections.emptyMap();
	}

	@Override
	protected Object getRawRequest() {
		return this.requestData;
	}

	@Override
	public boolean isSecure() {
		return true;
	}

}
