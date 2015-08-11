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
package com.threewks.thundr.test.mock.servlet;

import static com.atomicleopard.expressive.Expressive.list;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.http.ContentType;

// TODO - v3 - consolidate 'with' style setters and apply normal getters

public class MockHttpServletRequest implements HttpServletRequest {
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, String[]> parameters = new HashMap<String, String[]>();
	private Map<String, String[]> headers = new HashMap<String, String[]>();
	private String characterEncoding = "utf-8";
	private String contentType = null;
	private String protocol = "http";
	private String method = "GET";
	private String host = "localhost";
	private String path = "/";
	private String queryString = "";
	private HttpSession session;
	private String content;
	private RequestDispatcher requestDispatcher = new MockRequestDispatcher();
	private String serverName;
	private List<Cookie> cookies = list();
	private URL url;
	private Boolean inputStreamUsed = null;

	public MockHttpServletRequest() {
		url(path);
	}

	public MockHttpServletRequest(String url) {
		url(url);
	}

	public MockHttpServletRequest content(String content) {
		this.content = content;
		return this;
	}

	public MockHttpServletRequest method(String method) {
		this.method = method;
		return this;
	}

	public MockHttpServletRequest url(String url) {
		try {
			if (url.startsWith("/")) {
				url = String.format("%s://%s%s", protocol, host, url);
			}
			this.url = new URL(url);
			this.protocol = this.url.getProtocol();
			this.host = this.url.getHost();
			this.path = this.url.getPath();
			this.queryString = this.url.getQuery();
		} catch (MalformedURLException e) {
			throw new RuntimeException(url + " is not a valid URL or path.", e);
		}
		return this;
	}

	public MockHttpServletRequest session(HttpSession session) {
		this.session = session;
		return this;
	}

	public MockHttpServletRequest attribute(String name, Object value) {
		attributes.put(name, value);
		return this;
	}

	public MockHttpServletRequest attributes(Map<String, Object> attributes) {
		this.attributes.putAll(attributes);
		return this;
	}

	public MockHttpServletRequest parameter(String name, String value) {
		parameters.put(name, new String[] { value });
		return this;
	}

	public MockHttpServletRequest parameter(String name, String... values) {
		parameters.put(name, values);
		return this;
	}

	public MockHttpServletRequest parameters(Map<String, String[]> parameters) {
		this.parameters.putAll(parameters);
		return this;
	}

	public MockHttpServletRequest header(String name, String value) {
		headers.put(name, new String[] { value });
		return this;
	}

	public MockHttpServletRequest header(String name, String... values) {
		headers.put(name, values);
		return this;
	}

	public MockHttpServletRequest header(Map<String, String[]> headers) {
		this.headers.putAll(headers);
		return this;
	}

	public MockHttpServletRequest contentType(ContentType contentType) {
		this.contentType = contentType.value();
		return this;
	}

	public MockHttpServletRequest contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	public MockHttpServletRequest encoding(String encoding) {
		this.characterEncoding = encoding;
		return this;
	}

	public MockHttpServletRequest serverName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		this.characterEncoding = env;
	}

	@Override
	public int getContentLength() {
		return this.content == null ? 0 : content.getBytes().length;
	}

	@Override
	public long getContentLengthLong() {
		return this.content == null ? 0 : content.getBytes().length;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (Boolean.FALSE.equals(inputStreamUsed)) {
			throw new IOException("Called getInputStream() after getReader() was called");
		}
		inputStreamUsed = true;
		String body = (content == null) ? "" : content;
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(this.getCharacterEncoding()));
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return inputStream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
			}
		};
	}

	@Override
	public String getParameter(String name) {
		String[] parameterArray = parameters.get(name);
		return parameterArray != null && parameterArray.length > 0 ? parameterArray[0] : null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameters.get(name);
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	@Override
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public int getServerPort() {
		return 80;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (Boolean.TRUE.equals(inputStreamUsed)) {
			throw new IOException("Called getReader() after getInputStream() was called");
		}
		inputStreamUsed = false;
		return new BufferedReader(new StringReader(content));
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return Collections.enumeration(Collections.emptyList());
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		if (requestDispatcher() != null) {
			requestDispatcher().lastPath(path);
		}
		return requestDispatcher;
	}

	public void requestDispatcher(RequestDispatcher requestDispatcher) {
		this.requestDispatcher = requestDispatcher;
	}

	public MockRequestDispatcher requestDispatcher() {
		return Cast.as(requestDispatcher, MockRequestDispatcher.class);
	}

	@Override
	public String getRealPath(String path) {
		return path;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 80;
	}

	@Override
	public String getAuthType() {
		return null;
	}

	public void cookie(Cookie cookie) {
		cookies.add(cookie);
	}

	public void cookie(String name, String value) {
		cookies.add(new Cookie(name, value));
	}

	public String cookie(String name) {
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public void clearCookies() {
		cookies.clear();
	}

	@Override
	public Cookie[] getCookies() {
		return cookies.toArray(new Cookie[0]);
	}

	@Override
	public long getDateHeader(String name) {
		return 0;
	}

	@Override
	public String getHeader(String name) {
		Enumeration<String> headers = getHeaders(name);
		return headers.hasMoreElements() ? headers.nextElement() : null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		// @formatter:off
		Entry<String, String[]> entry = headers
				.entrySet()
				.stream()
				.filter(e -> e.getKey().equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
		// @formatter:on
		String[] values = entry == null ? null : entry.getValue();
		return values == null ? Collections.emptyEnumeration() : Collections.enumeration(list(values));
	}

	@Override
	public int getIntHeader(String name) {
		return 0;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return path;
	}

	@Override
	public String getPathTranslated() {
		return path;
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public boolean isUserInRole(String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return path;
	}

	@Override
	public StringBuffer getRequestURL() {
		String url = this.url.toString();
		int index = url.indexOf('?');
		if (index != -1) {
			url = url.substring(0, index);
		}
		return new StringBuffer(url);
	}

	@Override
	public String getServletPath() {
		return "/";
	}

	@Override
	public HttpSession getSession(boolean create) {
		return session == null && create ? new MockHttpSession() : session;
	}

	@Override
	public HttpSession getSession() {
		return session;
	}

	@Override
	public ServletContext getServletContext() {
		return session == null ? null : session.getServletContext();
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	@Override
	public String changeSessionId() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return true;
	}

	@Override
	public void login(String username, String password) throws ServletException {
	}

	@Override
	public void logout() throws ServletException {
	}

	// TODO - v3 - if we can support parts natively we can clean up the multipart binder a lot
	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return null;
	}
}
