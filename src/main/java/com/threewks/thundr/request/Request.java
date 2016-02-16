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

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.threewks.thundr.Experimental;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;

public interface Request {

	/**
	 * Gets the raw underlying request, or null if the given type cannot be converted from the underlying request
	 * 
	 * @param type
	 * @return
	 */
	public <T> T getRawRequest(Class<T> type);

	/**
	 * Returns a unique id for this request, useful for request correlation.
	 */
	public UUID getId();

	public String getContentTypeString();

	public ContentType getContentType();

	public String getCharacterEncoding();

	/**
	 * Return the first (or only) value for the given header. Header names are case-insensitive.
	 * 
	 * @param name
	 * @return
	 */
	public String getHeader(String name);

	/**
	 * Return the all values for the given header, or an empty list if none. Header names are case-insensitive.
	 * 
	 * @param name
	 * @return
	 */
	public List<String> getHeaders(String name);

	public Map<String, List<String>> getAllHeaders();

	public String getParameter(String name);

	public List<String> getParameters(String name);

	public Map<String, List<String>> getAllParameters();

	/**
	 * Add arbitraty data to the request scope
	 * 
	 * @param key
	 * @param value
	 */
	public void putData(String key, Object value);

	/**
	 * Put a set of arbitraty data on the request scope
	 * 
	 * @param values
	 */
	public void putData(Map<String, Object> values);

	/**
	 * Get all data set on the request scope
	 * 
	 * @return
	 */
	public Map<String, Object> getAllData();

	/**
	 * Get data from the request scope
	 * 
	 * @param key
	 * @return
	 */
	public <T> T getData(String key);

	public long getContentLength();

	public HttpMethod getMethod();

	public boolean isA(HttpMethod method);

	public boolean isSecure();

	/**
	 * Return a URI of the request the client made, callers can retrieve the individual components directly from the URI.
	 * 
	 * @return
	 */
	@Experimental
	public URI getRequestUri();

	/**
	 * Returns the path of the request
	 * 
	 * That is given <code>https://www.domain.com/path/to/resource#anchor?k=v</code> this method will
	 * return <code>/path/to/resource</code>
	 * 
	 * @return
	 */
	public String getRequestPath();

	/**
	 * Returns the route this request matches to, if any
	 * 
	 * @return
	 */
	public Route getRoute();

	public Cookie getCookie(String name);

	public Map<String, List<Cookie>> getAllCookies();

	public Reader getReader();

	public InputStream getInputStream();
}
