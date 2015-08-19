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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.StatusCode;

public interface Response {

	/**
	 * Attempt to get the underlying raw response object as the given type.
	 * 
	 * @param type
	 * @return the requested raw response or null if the given type does not match.
	 */
	public <T> T getRawResponse(Class<T> type);

	/**
	 * Returns true if the response is no longer open (i.e. has been handled already)
	 * 
	 * @return
	 */
	public boolean isCommitted();

	/**
	 * Returns true if the response is still open (i.e. has not been handled already)
	 * 
	 * @return
	 */
	public boolean isUncommitted();

	/**
	 * Specify the given header has the provided values, replaces all existing header values for this header
	 * 
	 * @param header
	 * @param value
	 * @return
	 */
	Response withHeader(String header, Object value);

	/**
	 * Specify the given header has the provided value if 'include' evaluates to true, replacing all existing header values for this header
	 * If include is false, this method is a noop
	 * 
	 * @param header
	 * @param value
	 * @param omit
	 * @return
	 */

	Response withHeader(String header, Object value, boolean include);

	Response withHeader(String header, Object... values);

	Response withHeader(String header, Collection<Object> values);

	Response withHeader(String header, Collection<Object> values, boolean include);

	Response withHeaders(Map<String, Object> headers);

	Response withStatusCode(StatusCode statusCode);

	Response withStatusCode(StatusCode statusCode, boolean include);

	Response withStatusMessage(String message);

	Response withContentType(String contentType);

	Response withContentType(ContentType contentType);

	Response withContentType(String contentType, boolean include);

	Response withCookie(Cookie cookies);

	Response withCookies(Cookie... cookies);

	Response withCookies(Collection<Cookie> cookies);

	Response withCharacterEncoding(String characterEncoding);

	Response withCharacterEncoding(String characterEncoding, boolean include);

	Response withContentLength(int length);

	Response withBody(String body);

	Response withBody(byte[] body);

	OutputStream getOutputStream() throws IOException;

	// Response withContent(byte[] data) throws IOException;
	//
	// Response withContent(InputStream inputStream) throws IOException;

	/*
	 * TODO - v3 - getters on the response? Makes it easier on testing, but is it right?
	 * String getHeader(String name);
	 * List<String> getHeaders(String name);
	 * Map<String, List<String>> getAllHeaders();
	 * StatusCode getStatusCode();
	 * ContentType getContentType();
	 * String getContentTypeString();
	 * Cookie getCookie(String name);
	 * List<Cookie> getCookies(String name);
	 * List<Cookie> getAllCookies();
	 * String getCharacterEncoding();
	 * long getContentLength();
	 */
	String getHeader(String name);

	List<String> getHeaders(String name);

	Map<String, List<String>> getAllHeaders();

	Cookie getCookie(String name);

	List<Cookie> getAllCookies();
}
