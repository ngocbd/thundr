/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.view;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.http.Cookies;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.view.redirect.RedirectView;
import com.threewks.thundr.view.redirect.RouteRedirectView;

/**
 * {@link BaseView} provides common functionality for adding metainformation to the
 * response server to the client.
 * Varying {@link ViewResolver} implementations may choose to ignore some values if they do
 * not make sense.
 * 
 * For advanced usages, you may need to receive the {@link HttpServletResponse} directly in your
 * controller method and manipulate it directly.
 */
public abstract class BaseView<Self extends BaseView<Self>> implements View {

	private Map<String, String> headers = new LinkedHashMap<String, String>();
	private Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
	private Integer statusCode;
	private String contentType;
	private String characterEncoding;
	@SuppressWarnings("unchecked") private Self self = (Self) this;

	/**
	 * Sets the specified header in the response
	 * 
	 * @param name
	 * @param value
	 * @return this view
	 */
	public Self withHeader(String name, String value) {
		headers.put(name, value);
		return self;
	}

	/**
	 * @param name
	 * @return the value of the header previously set into this view, or null
	 */
	public String getHeader(String name) {
		return headers.get(name);
	}

	/**
	 * Sets the specified cookie into the response
	 * 
	 * @param name
	 * @param value
	 * @return this view
	 */
	public Self withCookie(String name, String value) {
		return withCookie(Cookies.build(name).withValue(value).build());
	}

	/**
	 * Sets the specified cookie into the response
	 * 
	 * @param cookie
	 * @return this view
	 * @see Cookies for convenient cookie builder
	 */
	public Self withCookie(Cookie cookie) {
		cookies.put(cookie.getName(), cookie);
		return self;
	}

	/**
	 * @param name
	 * @return the cookie previously set into this view, or null
	 */
	public Cookie getCookie(String name) {
		return cookies.get(name);
	}

	/**
	 * @return the full set of cookies already added - alterations directly affect the underlying collection
	 */
	public Map<String, Cookie> getCookies() {
		return cookies;
	}

	/**
	 * @return the full set of headers already added - alterations directly affect the underlying collection
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Sets the response code of the response.
	 * Care needs to be taken to use this appropriately.
	 * 
	 * For example, to send a redirect, prefer a {@link RedirectView} or {@link RouteRedirectView}
	 * 
	 * To send an error:
	 * <ul>
	 * <li>To use the containers configured error page, throw {@link HttpStatusException} or setting {@link HttpServletResponse#sendError(int)}</li>
	 * <li>To send an error with your view as content, use this method and set an error status code</li>
	 * </ul>
	 * 
	 * 
	 * @param code
	 * @return this view
	 */
	public Self withStatusCode(int code) {
		this.statusCode = code;
		return self;
	}

	/**
	 * Sets the response code of the response.
	 * Care needs to be taken to use this appropriately.
	 * 
	 * For example, to send a redirect, prefer a {@link RedirectView} or {@link RouteRedirectView}
	 * 
	 * To send an error:
	 * <ul>
	 * <li>To use the containers configured error page, throw {@link HttpStatusException} or setting {@link HttpServletResponse#sendError(int)}</li>
	 * <li>To send an error with your view as content, use this method and set an error status code</li>
	 * </ul>
	 * 
	 * @param code
	 * @return this view
	 */
	public Self withStatusCode(StatusCode code) {
		return withStatusCode(code.getCode());
	}

	/**
	 * @return the response code previously set into this view
	 */
	public Integer getStatusCode() {
		return statusCode;
	}

	/**
	 * Sets the content type into the response
	 * 
	 * @param contentType
	 * @return this view
	 */
	public Self withContentType(String contentType) {
		this.contentType = contentType;
		return self;
	}

	/**
	 * @return the content type previously set into this view
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Set the character encoding of the response
	 * 
	 * @param characterEncoding
	 * @return this view
	 */
	public Self withCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
		return self;
	}

	/**
	 * @return the character encoding previously set into this view
	 */
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public static void applyToResponse(BaseView<?> view, HttpServletResponse resp) {
		for (Cookie cookie : view.getCookies().values()) {
			resp.addCookie(cookie);
		}
		for (Map.Entry<String, String> header : view.getHeaders().entrySet()) {
			resp.addHeader(header.getKey(), header.getValue());
		}
		Integer statusCode = view.getStatusCode();
		String contentType = view.getContentType();
		String characterEncoding = view.getCharacterEncoding();
		if (statusCode != null) {
			resp.setStatus(statusCode);
		}
		if (contentType != null) {
			resp.setContentType(contentType);
		}
		if (characterEncoding != null) {
			resp.setCharacterEncoding(characterEncoding);
		}
	}

	public static void includeModelInRequest(HttpServletRequest req, Map<String, Object> model) {
		for (Map.Entry<String, Object> modelEntry : model.entrySet()) {
			req.setAttribute(modelEntry.getKey(), modelEntry.getValue());
		}
	}
}
