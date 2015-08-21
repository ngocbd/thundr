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
package com.threewks.thundr.request;

import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.http.Cookie;
// TODO - v3 - this can be deleted if not used
public class ReadOnlyRequest extends BaseRequest {
	private Request request;
	protected Map<String, Object> data = new LinkedHashMap<>();

	public ReadOnlyRequest(Request request) {
		super(request.getMethod(), null);
		this.request = request;
	}

	@Override
	public Map<String, Object> getAllData() {
		Map<String, Object> allData = new LinkedHashMap<>();
		allData.putAll(this.request.getAllData());
		allData.putAll(this.data);
		return allData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getData(String key) {
		if(this.data.containsKey(key)){
			return (T) this.data.get(key);
		}else{
			return this.request.getData(key);
		}
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
	public String getContentTypeString() {
		return request.getContentTypeString();
	}

	@Override
	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public List<String> getHeaders(String name) {
		return request.getHeaders(name);
	}

	@Override
	public Map<String, List<String>> getAllHeaders() {
		return request.getAllHeaders();
	}

	@Override
	public String getParameter(String name) {
		return request.getParameter(name);
	}

	@Override
	public List<String> getParameters(String name) {
		return request.getParameters(name);
	}

	@Override
	public Map<String, List<String>> getAllParameters() {
		return request.getAllParameters();
	}

	@Override
	public long getContentLength() {
		return request.getContentLength();
	}

	@Override
	public String getRequestPath() {
		return request.getRequestPath();
	}

	@Override
	public Cookie getCookie(String name) {
		return request.getCookie(name);
	}

	@Override
	public Map<String, List<Cookie>> getAllCookies() {
		return request.getAllCookies();
	}

	@Override
	public Reader getReader() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	protected Object getRawRequest() {
		return null;
	}
}
