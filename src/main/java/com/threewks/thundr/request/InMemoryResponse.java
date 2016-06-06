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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.transformer.TransformerManager;

public class InMemoryResponse extends BaseResponse implements Response {
	protected Map<String, List<String>> headers = new LinkedHashMap<>();
	protected List<Cookie> cookies = new ArrayList<>();
	protected StatusCode statusCode = StatusCode.OK;
	protected String statusMessage;
	protected String contentType;
	protected String characterEncoding = "UTF-8";
	protected Long contentLength;
	protected ByteArrayOutputStream output = new ByteArrayOutputStream();

	public InMemoryResponse(TransformerManager transformerManager) {
		super(transformerManager);
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public Response withBody(String body) {
		try {
			byte[] data = body.getBytes(characterEncoding);
			return withBody(data);
		} catch (UnsupportedEncodingException e) {
			throw new BaseException(e);
		}
	}

	@Override
	protected void setHeaderInternal(String key, List<String> values) {
		headers.put(key, values);
	}

	@Override
	public Response withStatusCode(StatusCode statusCode) {
		this.statusCode = statusCode;
		return this;
	}

	@Override
	public Response withStatusMessage(String message) {
		this.statusMessage = message;
		return this;
	}

	@Override
	public Response withContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public Response withCookie(Cookie cookie) {
		this.cookies.add(cookie);
		return this;
	}

	@Override
	public Response withCookies(Collection<Cookie> cookies) {
		this.cookies.addAll(cookies);
		return this;
	}

	@Override
	public Response withCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
		return this;
	}

	@Override
	public Response withContentLength(long length) {
		this.contentLength = length;
		return this;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return output;
	}

	@Override
	protected Object getRawResponse() {
		return null;
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
	public StatusCode getStatusCode() {
		return statusCode;
	}

	@Override
	public ContentType getContentType() {
		return ContentType.from(contentType);
	}

	@Override
	public String getContentTypeString() {
		return contentType;
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public String getBodyAsString() {
		try {
			return new String(output.toByteArray(), characterEncoding);
		} catch (UnsupportedEncodingException e) {
			throw new BaseException(e);
		}
	}

	public byte[] getBodyAsBytes() {
		return output.toByteArray();
	}

	@Override
	public Long getContentLength() {
		return contentLength;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	@Override
	public Cookie getCookie(String name) {
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		return null;
	}

	@Override
	public List<Cookie> getAllCookies() {
		return Collections.unmodifiableList(this.cookies);
	}

	@Override
	public List<Cookie> getCookies(String name) {
		List<Cookie> results = new ArrayList<>();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				results.add(cookie);
			}
		}
		return results;
	}

	@Override
	public Response finaliseHeaders() {
		return this;
	}

}
