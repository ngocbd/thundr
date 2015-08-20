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

import static com.atomicleopard.expressive.Expressive.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.ETransformer;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.transformer.TransformerManager;

public abstract class BaseResponse implements Response {
	protected TransformerManager transformerManager;

	public BaseResponse(TransformerManager transformerManager) {
		super();
		this.transformerManager = transformerManager;
	}

	protected abstract Object getRawResponse();

	protected abstract void setHeaderInternal(String key, List<String> values);

	@Override
	public boolean isUncommitted() {
		return !isCommitted();
	}

	@Override
	public <T> T getRawResponse(Class<T> type) {
		return Cast.as(getRawResponse(), type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response withHeader(String header, Object value) {
		ETransformer<Object, String> transformer = (ETransformer<Object, String>) transformerManager.getBestTransformer(value.getClass(), String.class);
		String stringValue = transformer.from(value);
		setHeaderInternal(header, Collections.singletonList(stringValue));
		return this;
	}

	@Override
	public Response withHeader(String header, Object value, boolean include) {
		return include ? withHeader(header, value) : this;
	}

	@Override
	public Response withHeader(String header, Object... values) {
		return withHeader(header, Arrays.asList(values));
	}

	@Override
	public Response withHeader(String header, Collection<Object> values, boolean include) {
		return include ? withHeader(header, values) : this;
	}

	@Override
	public String getHeader(String name) {
		List<String> values = getHeaders(name);
		return isEmpty(values) ? null : values.get(0);
	}

	@Override
	public Response withBody(byte[] body) {
		try {
			getOutputStream().write(body);
			return this;
		} catch (IOException e) {
			throw new BaseException(e);
		}
	}

	@Override
	public Response withHeaders(Map<String, Object> headers) {
		for (Map.Entry<String, Object> entry : headers.entrySet()) {
			withHeader(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response withHeader(String header, Collection<Object> values) {
		List<String> stringValues = new ArrayList<>(values.size());
		for (Object value : values) {
			ETransformer<Object, String> transformer = (ETransformer<Object, String>) transformerManager.getBestTransformer(value.getClass(), String.class);
			stringValues.add(transformer.from(value));
		}
		setHeaderInternal(header, stringValues);
		return this;
	}

	@Override
	public Response withStatusCode(StatusCode statusCode, boolean include) {
		return include ? withStatusCode(statusCode) : this;
	}

	@Override
	public Response withContentType(ContentType contentType) {
		return withContentType(contentType.value());
	}

	@Override
	public Response withContentType(String contentType, boolean include) {
		return include ? withContentType(contentType) : this;
	}

	@Override
	public Response withCookies(Cookie... cookies) {
		return withCookies(Arrays.asList(cookies));
	}

	@Override
	public Response withCharacterEncoding(String characterEncoding, boolean include) {
		return include ? withCharacterEncoding(characterEncoding) : this;
	}
}
